package ord.zsd.ts2.flow

import better.files.File
import cats._
import cats.data.{State, WriterT}
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter.{MediaDb, MediaDbStore}
import ord.zsd.ts2.interpreter.omdb.HttpOMDbApiInterpreter
import ord.zsd.ts2.interpreter.parse.ParseInterpreter
import ord.zsd.ts2.interpreter.seriesdb.SeriesDbInterpreter
import ord.zsd.ts2.interpreter.seriesdb.SeriesDbInterpreter.Logging
import ord.zsd.ts2.interpreter.store.StoreInterpreter
import ord.zsd.ts2.mdb._
import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.parse.ParseOp
import ord.zsd.ts2.seriesdb._
import ord.zsd.ts2.store.StoreOp
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.interpret._
import org.atnos.eff.syntax.all._

import scala.concurrent.Future

object SeriesDbFlow {

  // ---------------------------------------------------------------------------------------------------------------

  type InitialStack = Fx.fx7[SeriesDbOp, OMDbOp, MediaDbOp, MediaDbStore, ParseOp, List, Logging]

  //noinspection TypeAnnotation
  def runWithInMemoryStore(program: Eff[InitialStack, Unit]) = {
    val step1 = SeriesDbInterpreter.translate(program)

    StoreMediaDbInterpreter.translate(step1)
      .transform(HttpOMDbApiInterpreter.interpret)
      .transform(StoreInterpreter.interpretToState[MediaDb])
      .transform(ParseInterpreter.interpret)
  }

  //noinspection TypeAnnotation
  def runWithDiskStore(folderChangedEvent: FolderChanged)(path: File)(empty: MediaDb) = {

    import spray.json._
    import fommil.sjs.FamilyFormats._
    import ord.zsd.ts2.eff._

    val program = send[SeriesDbOp, InitialStack, Unit](UpdateForFolderChanged(folderChangedEvent))

    val step1 = SeriesDbInterpreter.translate(program)

    StoreMediaDbInterpreter.translate(step1)
      .transform(StoreInterpreter.interpretToJsonFileEval[MediaDb](path)(empty))
      .transmorph(ParseInterpreter.interpret)
      .transform(HttpOMDbApiInterpreter.interpret)
  }
}
