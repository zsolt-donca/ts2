package ord.zsd.ts2.flow

import ord.zsd.ts2.files.FilesOp._filesOp
import ord.zsd.ts2.files.{ListRecursively, MediaPath}
import ord.zsd.ts2.mdb.{EpisodeMedia, Media, MovieMedia}
import ord.zsd.ts2.seriesdb.SeriesDbOp._seriesDbOp
import ord.zsd.ts2.seriesdb._
import org.atnos.eff._
import org.atnos.eff.all._

object SyncMediaDb {

  def syncMediaDb[R: _filesOp : _seriesDbOp : _list](seriesFolder: String): Eff[R, Unit] = {
    for {
      folderContents <- send(ListRecursively(seriesFolder))
      dbContents <- send(ReadMediaDb())
      changeEvents = calculateDiff(folderContents, dbContents)
      changedEvent <- fromList(changeEvents)
      _ <- send(UpdateForFolderChanged(changedEvent))
    } yield ()
  }

  private def calculateDiff(folderContents: List[MediaPath], dbContents: List[Media]): List[FolderChanged] = {
    val folderContentsByPath = folderContents.toSet
    val dbContentsPaths = dbContents.collect {
      case EpisodeMedia(_, _, _, _, path) => path
      case MovieMedia(_, _, path) => path
    }.toSet

    val added = folderContentsByPath -- dbContentsPaths
    val removed = dbContentsPaths -- folderContentsByPath

    val folderChanged = added.map(path => FolderChanged(path, Added)) ++ removed.map(path => FolderChanged(path, Removed))
    folderChanged.toList
  }

}
