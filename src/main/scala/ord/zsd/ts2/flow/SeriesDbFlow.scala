package ord.zsd.ts2.flow

import better.files.File
import cats._
import cats.data.State
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
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.interpret._
import org.atnos.eff.syntax.all._

import scala.concurrent.Future

object SeriesDbFlow {

  // ---------------------------------------------------------------------------------------------------------------

  type Stack1 = Fx.fx5[Eval, State[MediaDb, ?], Future, List, Logging]

  def run1(folderChangedEvent: FolderChanged): Eff[Stack1, Unit] = {

    type InitialStack = Fx.fx7[SeriesDbOp, OMDbOp, MediaDbOp, MediaDbStore, ParseOp, List, Logging]
    val program = send[SeriesDbOp, InitialStack, Unit](UpdateForFolderChanged(folderChangedEvent))

    val step1 = SeriesDbInterpreter.translate(program)

    StoreMediaDbInterpreter.translate(step1)
      .transform(HttpOMDbApiInterpreter.interpret)
      .transform(StoreInterpreter.interpretToState[MediaDb])
      .transform(ParseInterpreter.interpret)
  }

  type Stack2 = Fx.fx4[Future, Eval, List, Logging]

  def run2(folderChangedEvent: FolderChanged)(path: File)(empty: MediaDb): Eff[Stack2, Unit] = {

    import fommil.sjs.FamilyFormats._
    import ord.zsd.ts2.eff._

    type InitialStack = Fx.fx8[SeriesDbOp, OMDbOp, MediaDbOp, MediaDbStore, Eval, ParseOp, List, Logging]
    val program = send[SeriesDbOp, InitialStack, Unit](UpdateForFolderChanged(folderChangedEvent))

    val step1 = SeriesDbInterpreter.translate(program)

    StoreMediaDbInterpreter.translate(step1)
      .transmorph(StoreInterpreter.interpretToJsonFileEval[MediaDb](path)(empty))
      .transmorph(ParseInterpreter.interpret)
      .transform(HttpOMDbApiInterpreter.interpret)
  }
}
