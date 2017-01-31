package ord.zsd.ts2

import cats.~>
import org.atnos.eff.Interpret.translate
import org.atnos.eff.all.send
import org.atnos.eff.{Eff, Member, Translate, |=}

import scala.language.higherKinds

package object eff {
  /**
    * Transform + Translate
    *
    * TODO make pull request - this looks like a genuinely useful operation
    */
  def transmorph[R, U, A, F[_], G[_]](e: Eff[R, A])(interpret: F ~> G)(implicit m: Member.Aux[F, R, U], gu: G |= U) : Eff[U, A] = {
    translate(e)(new Translate[F, U] {
      override def apply[X](kv: F[X]): Eff[U, X] = {
        send(interpret(kv))
      }
    })
  }

  implicit class TranslatePolyOps[R, A](val e: Eff[R, A]) extends AnyVal {
    def transmorph[U, F[_], G[_]](interpret: F ~> G)(implicit m: Member.Aux[F, R, U], gu: G |= U) : Eff[U, A] = {
      eff.transmorph(e)(interpret)
    }
  }
}
