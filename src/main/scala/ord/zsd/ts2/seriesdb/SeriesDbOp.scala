package ord.zsd.ts2.seriesdb

import ord.zsd.ts2.mdb.MediaPath
import org.atnos.eff.|=

sealed trait SeriesDbOp[A]

object SeriesDbOp {
  type _seriesDbOp[R] = SeriesDbOp |= R
}

sealed trait ChangeType
case object Added extends ChangeType
case object Removed extends ChangeType

case class FolderChanged(path: MediaPath, changeType: ChangeType)

case class UpdateForFolderChanged(folderChanged: FolderChanged) extends SeriesDbOp[Unit]
