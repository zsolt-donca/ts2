package ord.zsd.ts2.interpreter

import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

import scala.language.higherKinds

sealed trait StoreOp[S, A]

case class ReadStore[S]() extends StoreOp[S, S]

case class WriteStore[S](s: S) extends StoreOp[S, Unit]

object StoreOp {

  def readStore[S, R](implicit member: StoreOp[S, ?] |= R): Eff[R, S] =
    send[StoreOp[S, ?], R, S](ReadStore())

  def writeStore[S, R](s: S)(implicit member: StoreOp[S, ?] |= R): Eff[R, Unit] =
    send[StoreOp[S, ?], R, Unit](WriteStore(s))
}