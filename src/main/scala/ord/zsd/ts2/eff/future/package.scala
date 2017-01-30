package ord.zsd.ts2.eff

import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.interpret._
import org.atnos.eff.syntax.all._
import cats.{Applicative, Traverse}
import cats.data._
import cats.implicits._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.higherKinds

package object future {
  type _future[R] = Future |= R

  def ApplicativeFuture(implicit ec: ExecutionContext): Applicative[Future] = new Applicative[Future] {
    def pure[A](x: A): Future[A] =
      Future.successful(x)

    def ap[A, B](ff: Future[A => B])(fa: Future[A]): Future[B] =
      fa.zip(ff).map { case (a, f) => f(a) }
  }

  def future[R: _future, A](a: => A)(implicit ec: ExecutionContext): Eff[R, A] =
    send[Future, R, A](Future(a))

  def runFuture[R, U, A, B](atMost: Duration)(effects: Eff[R, A])(implicit m: Member.Aux[Future, R, U], ec: ExecutionContext): Eff[U, A] = {

    val recurse = new Recurse[Future, U, A] {
      def apply[X](m: Future[X]): X Either Eff[U, A] =
        Left(Await.result(m, atMost))

      def applicative[X, T[_]: Traverse](ms: T[Future[X]]): T[X] Either Future[T[X]] =
        Right(ApplicativeFuture.sequence(ms))

    }
    
    interpret1((a: A) => a)(recurse)(effects)
  }

  implicit class EffFutureOps[R, A](eff: Eff[R, A]) {
    def runFuture[U, B](atMost: Duration)(implicit m: Member.Aux[Future, R, U], ec: ExecutionContext): Eff[U, A] = {
      ord.zsd.ts2.eff.future.runFuture[R, U, A, B](atMost)(eff)
    }
  }
}
