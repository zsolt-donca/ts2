package apps

object Eff3TransformDSL {

  import org.atnos.eff._
  import org.atnos.eff.eff._
  import org.atnos.eff.syntax.eff._
  import org.atnos.eff.async._
  import org.atnos.eff.interpret._

  // list of access rights for a valid token
  case class AccessRights(rights: List[String])

  // authentication error
  case class AuthError(message: String)

  // DSL for authenticating users
  sealed trait Authenticated[A]
  case class Authenticate(token: String) extends Authenticated[AccessRights]

  type AuthErrorEither[A] = AuthError Either A
  type _error[R] = AuthErrorEither |= R

  /**
    * The order of implicit parameters is really important for type inference!
    * see below
    */
  def runAuth[R, U, A](e: Eff[R, A])(implicit
                                     authenticated: Member.Aux[Authenticated, R, U],
                                     async: _async[U],
                                     either: _error[U]): Eff[U, A] =

    translate(e)(new Translate[Authenticated, U] {
      def apply[X](ax: Authenticated[X]): Eff[U, X] =
        ax match {
          case Authenticate(token) =>
            // send the async effect in the stack U
            send(authenticate(token)).
              // send the Either value in the stack U
              collapse
        }
    })

  // call to a service to authenticate tokens
  def authenticate(token: String): Async[AuthError Either AccessRights] = ???

  type S = Fx.fx3[Authenticated, AuthError Either ?, Async]

  def auth: Eff[S, Int] = ???

  runAuth(auth)

}
