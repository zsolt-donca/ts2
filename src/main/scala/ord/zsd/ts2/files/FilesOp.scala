package ord.zsd.ts2.files

import org.atnos.eff.|=

sealed trait FilesOp[A]

object FilesOp {
  type _filesOp[R] = FilesOp |= R
}

case class ListRecursively(directoryPath: String) extends FilesOp[List[MediaPath]]
