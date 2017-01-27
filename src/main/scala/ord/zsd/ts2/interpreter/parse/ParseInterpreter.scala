package ord.zsd.ts2.interpreter.parse

import cats.{Eval, ~>}
import ord.zsd.ts2.mdb.MediaDbBuilder
import ord.zsd.ts2.mdb.MediaDbBuilder.buildEpisode
import ord.zsd.ts2.parse.{ParseMovie, ParseOp, ParseSeries}

object ParseInterpreter {
  def interpret: ParseOp ~> Eval = {
    new (ParseOp ~> Eval) {
      override def apply[A](fa: ParseOp[A]): Eval[A] = fa match {
        case ParseSeries(path) => Eval.later({
          if (MediaDbBuilder.isTargetedFile(path))
            buildEpisode(path)
          else
            List()
        }).asInstanceOf[Eval[A]]
          
        case ParseMovie(path) => ???
      }
    }
  }
}
