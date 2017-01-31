package apps

import cats._
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._
import org.atnos.eff.Interpret._
import ord.zsd.ts2.eff._

import scala.language.higherKinds

object EffTransform2 extends App {

  trait D1[A]
  case class Square[A](value: A)(implicit val numeric: Numeric[A]) extends D1[A]
  type _d1[R] = D1 |= R

  trait D2[A]
  case class Negate[A](value: A)(implicit val numeric: Numeric[A]) extends D2[A]
  type _d2[R] = D2 |= R

  def justSquare[R: _d1, A: Numeric](value: A): Eff[R, A] = send[D1, R, A](Square(value))

  def justNegate[R: _d2, A: Numeric](value: A): Eff[R, A] = send[D2, R, A](Negate(value))

  def squareAndThenNegate[R: _d1 : _d2, A: Numeric](value: A): Eff[R, A] = {
    for {
      squared <- send[D1, R, A](Square(value))
      negated <- send[D2, R, A](Negate(squared))
    } yield negated
  }

  // ---------------------------- interpret ----------------------------

  def interpretD1[R: _d1, _eval]: D1 ~> Eval = new (D1 ~> Eval) {
    override def apply[A](fa: D1[A]): Eval[A] = fa match {
      case sq@Square(value) =>
        import sq.numeric._
        Eval.now(value * value)
    }
  }

  def interpretD2[R: _d2, _eval]: D2 ~> Eval = new (D2 ~> Eval) {
    override def apply[A](fa: D2[A]): Eval[A] = fa match {
      case neg@Negate(value) =>
        import neg.numeric._
        Eval.now(-value)
    }
  }

  def testSquareAndNegateWithTransform(): Unit = {
    type InitialStack = Fx.fx2[D1, D2]
    type FinalStack = Fx.fx2[Eval, Eval]

    val s: Eff[FinalStack, Int] = squareAndThenNegate[InitialStack, Int](7)
      .transform(interpretD1)
      .transform(interpretD2)

    val r: Int = s.runEval.runEval.run // sometimes it's not okay to have the same effect twice on the stack
    println(r)
  }

  def testJustSquareWithTransform(): Unit = {
    type InitialStack = Fx.fx1[D1]
    type FinalStack = Fx.fx1[Eval]

    val s: Eff[FinalStack, Int] = justSquare[InitialStack, Int](7)
      .transform(interpretD1)

    val r: Int = s.runEval.run
    println(r)
  }

  testSquareAndNegateWithTransform()
  testJustSquareWithTransform()

  // ---------------------------- translate ----------------------------

  def translateD1[R, U, A](e: Eff[R, A])(implicit d2: Member.Aux[D1, R, U], eval: _eval[U]): Eff[U, A] = {
    translate(e)(new Translate[D1, U] {
      def apply[X](ax: D1[X]): Eff[U, X] =
        ax match {
          case sq@Square(value) =>
            import sq.numeric._
            send(Eval.now(value * value))
        }
    })
  }

  def translateD2[R, U, A](e: Eff[R, A])(implicit d2: Member.Aux[D2, R, U], eval: _eval[U]): Eff[U, A] = {
    translate(e)(new Translate[D2, U] {
      def apply[X](ax: D2[X]): Eff[U, X] =
        ax match {
          case neg@Negate(value) =>
            import neg.numeric._
            send(Eval.now(-value))
        }
    })
  }


  def testSquareAndNegateWithEvalTranslate1(): Unit = {
    type InitialStack = Fx.fx3[D1, D2, Eval]
    type FinalStack = Fx.fx1[Eval]

    val s: Eff[FinalStack, Int] = translateD2(translateD1(squareAndThenNegate[InitialStack, Int](7)))

    val r: Int = s.runEval.run
    println(r)
  }

  def testSquareAndNegateWithEvalTranslate2(): Unit = {
    type InitialStack = Fx.fx2[D2, D1]
    type FinalStack = Fx.fx1[Eval]

    val s = squareAndThenNegate[InitialStack, Int](7)
      .transform(interpretD1)
      .transmorph(interpretD2)

    val r: Int = s.runEval.run
    println(r)
  }

  testSquareAndNegateWithEvalTranslate1()
}
