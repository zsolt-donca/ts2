package apps

object EffSafe extends App {

  import org.atnos.eff.syntax.all._
  import org.atnos.eff._, all._
  import cats.Eval

  // let's represent a resource which can be in use
  case class Resource(values: List[Int] = (1 to 10).toList, inUse: Boolean = false) {
    def isClosed = !inUse
  }

  var resource = Resource()

  // our stack of effects, with safe evaluation

  def openResource[R: _safe]: Eff[R, Resource] =
    protect {
      resource = resource.copy(inUse = true); resource
    }

  def closeResource[R: _safe](r: Resource): Eff[R, Unit] =
    protect(resource = r.copy(inUse = false))

  def useResource[R: _safe](ok: Boolean)(r: Resource): Eff[R, Int] =
    protect[R, Int](if (ok) r.values.sum else throw new Exception("boo"))

  // this program uses the resource safely even if there is an exception
  def program(ok: Boolean): (Throwable Either Int, List[Throwable]) = {
    type S = Fx.fx1[Safe]
    bracket(openResource[S])(useResource[S](ok))(closeResource[S]).
      runSafe.run
  }
}
