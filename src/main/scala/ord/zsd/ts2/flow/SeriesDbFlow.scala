package ord.zsd.ts2.flow

import better.files.File
import cats._
import cats.data.{State, Writer, WriterT}
import ord.zsd.ts2.interpreter.StoreOp
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter.{MediaDb, MediaDbStore, translateToStore}
import ord.zsd.ts2.interpreter.omdb.HttpOMDbApiInterpreter
import ord.zsd.ts2.interpreter.parse.ParseInterpreter
import ord.zsd.ts2.interpreter.store.StoreInterpreter
import ord.zsd.ts2.mdb.MediaDbOp._mediaDbOp
import ord.zsd.ts2.mdb._
import ord.zsd.ts2.omdbapi.OMDbOp._omdbOp
import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.parse.ParseOp._parseOp
import ord.zsd.ts2.parse.{ParseOp, ParseSeries}
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._
import org.atnos.eff.interpret._

import scala.concurrent.Future

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

  type Stack1 = Fx.fx5[Eval, State[MediaDb, ?], Future, List, Logging]
  def run1(folderChangedEvents: List[FolderChangedEvent]): Eff[Stack1, List[Media]] = {

    type InitialStack = Fx.fx6[OMDbOp, MediaDbOp, MediaDbStore, ParseOp, List, Logging]
    val initialEff = updateSeriesDbForEvents[InitialStack](folderChangedEvents)

    translateToStore(initialEff)
      .transform(HttpOMDbApiInterpreter.interpret)
      .transform(StoreInterpreter.interpretToState[MediaDb])
      .transform(ParseInterpreter.interpret)
  }

  type Stack2 = Fx.fx4[Future, Eval, List, Logging]
  def run2(folderChangedEvents: List[FolderChangedEvent])(path: File)(empty: MediaDb): Eff[Stack2, List[Media]] = {

    import ord.zsd.ts2.eff._
    import fommil.sjs.FamilyFormats._

    type InitialStack = Fx.fx7[OMDbOp, MediaDbOp, MediaDbStore, Eval, ParseOp, List, Logging]
    val initialEff = updateSeriesDbForEvents[InitialStack](folderChangedEvents)

    val step1 = translateToStore(initialEff)
    val step2 = transmorph(step1)(StoreInterpreter.interpretToJsonFile[MediaDb](path)(empty))
    val step3 = transmorph(step2)(ParseInterpreter.interpret)
    val step4 = transform(step3, HttpOMDbApiInterpreter.interpret)

    step4
  }
}
