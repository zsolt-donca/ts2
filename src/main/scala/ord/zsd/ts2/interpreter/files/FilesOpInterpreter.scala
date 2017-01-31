package ord.zsd.ts2.interpreter.files

import better.files.File
import cats.{Eval, ~>}
import ord.zsd.ts2.files.{FilesOp, ListRecursively, MediaPath}

/*_*/
object FilesOpInterpreter {

  def interpret: FilesOp ~> Eval = new (FilesOp ~> Eval) {
    override def apply[A](fa: FilesOp[A]): Eval[A] = {
      fa match {
        case ListRecursively(directoryPath) => Eval.always {
          val file = File(directoryPath)
          val files = file.listRecursively.toList

          val mediaPaths = files.map(f => MediaPath(f.pathAsString, f.isDirectory))

          mediaPaths
        }
      }
    }
  }

}
