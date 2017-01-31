package ord.zsd.ts2.app

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.event.Logging
import better.files.File
import better.files.File.home
import cats.Eval
import ord.zsd.ts2.app.Ts2AppActor.{FolderChangedAction, SyncDb}
import ord.zsd.ts2.flow.{SeriesDbFlow, SyncMediaDb}
import ord.zsd.ts2.flow.SeriesDbFlow.Stack2
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter.{MediaDb, MediaDbStore}
import ord.zsd.ts2.seriesdb.{FolderChanged, SeriesDbOp}
import org.atnos.eff.Eff
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.interpret._
import org.atnos.eff.syntax.all._
import ord.zsd.ts2.eff.future._
import ord.zsd.ts2.Ts2System._
import ord.zsd.ts2.files.FilesOp
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter
import ord.zsd.ts2.interpreter.omdb.HttpOMDbApiInterpreter
import ord.zsd.ts2.interpreter.parse.ParseInterpreter
import ord.zsd.ts2.interpreter.seriesdb.SeriesDbInterpreter
import ord.zsd.ts2.interpreter.store.StoreInterpreter
import ord.zsd.ts2.eff._
import ord.zsd.ts2.interpreter.files.FilesOpInterpreter
import ord.zsd.ts2.interpreter.seriesdb.SeriesDbInterpreter.Logging
import ord.zsd.ts2.mdb.MediaDbOp
import ord.zsd.ts2.omdbapi.OMDbOp
import ord.zsd.ts2.parse.ParseOp

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class Ts2AppActor extends Actor {
  private val log = Logging(context.system, this)

  private val jsonFile = home / ".ts2" / "db.json"

  override def receive: Receive = {

    case SyncDb(seriesFolder) =>
      import fommil.sjs.FamilyFormats._

      type InitialStack = Fx.fx9[FilesOp, SeriesDbOp, OMDbOp, MediaDbOp, MediaDbStore, Eval, ParseOp, List, Logging]
      val program: Eff[InitialStack, Unit] = SyncMediaDb.syncMediaDb[InitialStack](seriesFolder)

      val step1 = SeriesDbInterpreter.translate(program)

      type FinalStack = Fx.fx4[Future, Eval, List, Logging]
      val step2: Eff[FinalStack, Unit] = StoreMediaDbInterpreter.translate(step1)
        .transmorph(StoreInterpreter.interpretToJsonFileEval[MediaDb](jsonFile)(MediaDb.empty))
        .transmorph(ParseInterpreter.interpret)
        .transform(HttpOMDbApiInterpreter.interpret)
        .transmorph(FilesOpInterpreter.interpret)

      step2.runEval.runList.runWriterUnsafe[String](log.info).runFuture(30 seconds).run

    case FolderChangedAction(folderChanged) =>
      val eff: Eff[Stack2, Unit] = SeriesDbFlow.run2(folderChanged)(jsonFile)(MediaDb.empty)

      eff.runEval.runList.runWriterUnsafe[String](log.info).runFuture(30 seconds).run
  }
}

object Ts2AppActor {
  sealed trait Action
  case class SyncDb(seriesFolder: String) extends Action
  case class FolderChangedAction(folderChanged: FolderChanged) extends Action
}