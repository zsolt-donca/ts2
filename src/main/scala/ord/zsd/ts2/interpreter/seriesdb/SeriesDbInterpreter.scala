package ord.zsd.ts2.interpreter.seriesdb

import cats.data.Writer
import ord.zsd.ts2.mdb.MediaDbOp._mediaDbOp
import ord.zsd.ts2.mdb._
import ord.zsd.ts2.omdbapi.OMDbOp._omdbOp
import ord.zsd.ts2.omdbapi.{EpisodeByTitle, FindDetails, SeriesByTitle, SeriesType}
import ord.zsd.ts2.parse.ParseOp._parseOp
import ord.zsd.ts2.parse.ParseSeries
import ord.zsd.ts2.seriesdb._
import org.atnos.eff._
import org.atnos.eff.all.{_list, fromList, send, tell}

object SeriesDbInterpreter {

  type Logging[A] = Writer[String, A]
  type _logging[R] = Logging |= R

  def translate[R, U, A](e: Eff[R, A])(implicit member: Member.Aux[SeriesDbOp, R, U],
                                       p: _parseOp[U], m: _mediaDbOp[U], o: _omdbOp[U],
                                       l: _list[U], w: _logging[U]): Eff[U, A] = {
    interpret.translate(e)(new Translate[SeriesDbOp, U] {
      def apply[X](ax: SeriesDbOp[X]): Eff[U, X] = ax match {
        case UpdateForFolderChanged(folderChanged) =>
          updateSeriesDbForEvent(folderChanged).asInstanceOf[Eff[U, X]] // should not be necessary
      }
    })
  }

  private def updateSeriesDbForEvent[R: _parseOp : _mediaDbOp : _omdbOp : _list : _logging](folderChangedEvent: FolderChanged): Eff[R, Unit] = {
    folderChangedEvent.changeType match {
      case Added =>
        addToDb(folderChangedEvent.path)
      case Removed =>
        removeFromDb(folderChangedEvent.path)
    }
  }

  private def addToDb[R: _parseOp : _mediaDbOp : _omdbOp : _list : _logging](path: MediaPath): Eff[R, Unit] = {
    for {
      _ <- tell(s"Adding $path to the DB.")

      episodes <- send(ParseSeries(path))
      _ <- tell(s"Parsing $path was ${if (episodes.nonEmpty) s"successful: $episodes" else "failed"}.")

      episode <- fromList(episodes)
      _ <- addSeriesToDbIfNeeded(episode.seriesTitle)
      _ <- addEpisodeToDb(episode)
    } yield ()
  }

  private def addSeriesToDbIfNeeded[R: _mediaDbOp : _omdbOp : _logging](seriesTitle: String): Eff[R, Unit] = {
    for {
      mediaOption <- send(FindEntryByTitle(seriesTitle, SeriesType))

      _ <- if (mediaOption.isEmpty)
        for {
          _ <- tell(s"Series titled $seriesTitle is unknown.")

          findResponse <- send(FindDetails(SeriesByTitle(seriesTitle), Some(SeriesType)))
          _ <- findResponse match {
            case Left(error) => tell(s"Details of series titled $seriesTitle could not be obtained: $error")
            case Right(mediaDetails) => tell(s"Details of series titled $seriesTitle are: $mediaDetails")
          }

          _ <- send(SaveEntry(SeriesMedia(seriesTitle, findResponse.toOption)))
        } yield ()
      else
        for {
          _ <- tell(s"Series titled $seriesTitle is already known.")
        } yield ()
    } yield ()
  }

  private def addEpisodeToDb[R: _mediaDbOp : _omdbOp : _logging](episode: EpisodeMedia): Eff[R, Unit] = {
    for {
      findResponse <- send(FindDetails(EpisodeByTitle(episode.seriesTitle, episode.season, episode.episode)))
      _ <- findResponse match {
        case Left(error) => tell(s"Details of the episode $episode could not be obtained: $error")
        case Right(mediaDetails) => tell(s"Details of episode $episode are: $mediaDetails")
      }

      _ <- send(SaveEntry(episode.copy(details = findResponse.toOption)))
    } yield ()
  }

  private def removeFromDb[R: _mediaDbOp : _logging](path: MediaPath): Eff[R, Unit] = {
    for {
      _ <- tell(s"Removing path from DB: $path")
      _ <- send(DeleteEntriesByPath(path))
    } yield ()
  }
}
