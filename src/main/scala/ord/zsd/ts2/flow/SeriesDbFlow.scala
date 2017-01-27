package ord.zsd.ts2.flow

import cats.data.{StateT, Writer, WriterT}
import ord.zsd.ts2.mdb.MediaDbOp._mediaDbOp
import ord.zsd.ts2.mdb._
import ord.zsd.ts2.omdbapi.OMDbOp._omdbOp
import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.parse.ParseOp._parseOp
import ord.zsd.ts2.parse.{ParseOp, ParseSeries}
import org.atnos.eff._
import all._
import org.atnos.eff.syntax.all._
import cats._
import cats.syntax.all._
import cats.instances.all._
import cats.implicits._
import monix.eval.Task
import ord.zsd.ts2.interpreter.mdb.StateMediaDbInterpreter
import ord.zsd.ts2.interpreter.mdb.StateMediaDbInterpreter.MediaDbState
import ord.zsd.ts2.interpreter.omdb.HttpOMDbApiInterpreter
import ord.zsd.ts2.interpreter.parse.ParseInterpreter

object SeriesDbFlow {

  sealed trait ChangeType
  case object Added extends ChangeType
  case object Removed extends ChangeType

  case class FolderChangedEvent(path: MediaPath, changeType: ChangeType)

  type Logging[A] = Writer[String, A]
  type _logging[R] = Logging |= R

  // ---------------------------------------------------------------------------------------------------------------

  def updateSeriesDbForEvents[R: _parseOp : _mediaDbOp : _omdbOp : _list : _logging](folderChangedEvents: List[FolderChangedEvent]): Eff[R, List[Media]] = {
    for {
      folderChangedEvent <- values(folderChangedEvents: _*)
      _ <- updateSeriesDbForEvent(folderChangedEvent)
      medias <- send[MediaDbOp, R, List[Media]](FindAllEntries())
    } yield medias
  }

  def updateSeriesDbForEvent[R: _parseOp : _mediaDbOp : _omdbOp : _list : _logging](folderChangedEvent: FolderChangedEvent): Eff[R, Unit] = {
    folderChangedEvent.changeType match {
      case Added =>
        addToDb(folderChangedEvent.path)
      case Removed =>
        removeFromDb(folderChangedEvent.path)
    }
  }

  def addToDb[R: _parseOp : _mediaDbOp : _omdbOp : _list : _logging](path: MediaPath): Eff[R, Unit] = {
    for {
      _ <- tell(s"Adding $path to the DB.")

      episodes <- send(ParseSeries(path))

      _ <- tell(s"Parsing $path was ${if (episodes.nonEmpty) "successful" else "failed"}.")

      episode <- values(episodes: _*)

      seriesTitle = episode.seriesTitle

      _ <- tell(s"Episode is: $episode")

      _ <- addSeriesToDbIfNeeded(seriesTitle)

      findResponse <- send(FindDetails(EpisodeByTitle(seriesTitle, episode.season, episode.episode)))

      _ <- findResponse match {
        case Left(error) => tell(s"Details of the episode $episode could not be obtained: $error")
        case Right(mediaDetails) => tell(s"Details of episode $episode are: $mediaDetails")
      }

      _ <- send(SaveEntry(episode.copy(details = findResponse.toOption)))
    } yield ()
  }

  def addSeriesToDbIfNeeded[R: _mediaDbOp : _omdbOp : _logging](seriesTitle: String): Eff[R, Unit] = {
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

  def removeFromDb[R: _mediaDbOp : _logging](path: MediaPath): Eff[R, Unit] = {
    for {
      _ <- tell(s"Removing path from DB: $path")
      _ <- send(DeleteEntriesByPath(path))
    } yield ()
  }

  type FinalStack = Fx.fx5[Eval, MediaDbState, Async, List, Logging]

  def runWithStandardInterpreters(folderChangedEvents: List[FolderChangedEvent]): Eff[FinalStack, List[Media]] = {
    type InitialStack = Fx.fx5[OMDbOp, MediaDbOp, ParseOp, List, Logging]

    updateSeriesDbForEvents[InitialStack](folderChangedEvents)
      .transform(HttpOMDbApiInterpreter.interpret)
      .transform(StateMediaDbInterpreter.interpret)
      .transform(ParseInterpreter.interpret)
  }
}
