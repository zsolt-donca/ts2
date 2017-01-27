package ord.zsd.ts2.utils

import cats.syntax.all._
import cats.Monad

object MonadOps {

  def ifOption[M[_]: Monad, T, R](option: Option[T])(thenBranch: (T => M[R]))(elseBranch: M[R]): M[R] = {
    option.map(thenBranch).getOrElse(elseBranch)
  }

}
