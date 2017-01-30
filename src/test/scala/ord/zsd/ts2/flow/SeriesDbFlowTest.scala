package ord.zsd.ts2.flow


import ord.zsd.ts2.flow.SeriesDbFlow.{FolderChangedEvent, runWithStandardInterpreters}
import ord.zsd.ts2.interpreter.mdb.StateMediaDbInterpreter.MediaDb
import ord.zsd.ts2.mdb.{Media, MediaPath}
import org.atnos.eff._
import all._
import monix.eval.Task
import org.atnos.eff.syntax.all._
import org.scalatest.FunSuite
import org.atnos.eff.syntax.addon.monix._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._
import ord.zsd.ts2.eff.future._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


class SeriesDbFlowTest extends FunSuite {
  test("Simple stuff") {

    import SeriesDbFlow._
    import ord.zsd.ts2.Ts2System._

    val paths = List("american.horror.story.s01e02.repack.720p.bluray.x264-demand.mkv", "The.Night.Of.Part.6.1080i.HDTV.H.264.HUN-nIk.mkv")
    val folderChangedEvents: List[FolderChangedEvent] = paths.map(path => FolderChangedEvent(MediaPath(path, false), Added))

    val res: Eff[FinalStack, List[Media]] = SeriesDbFlow.runWithStandardInterpreters(folderChangedEvents)

    val emptyMediaDb: MediaDb = MediaDb(Vector.empty, 0)
    val (list: List[List[Media]], mediaDb: MediaDb) = res.runEval.runList.runState(emptyMediaDb).runWriterUnsafe((o: String) => println(o)).runFuture(10 seconds).run
    println(s"List: $list")
    println(s"Flat list: ${list.flatten}")
    println(s"MediaDb: $mediaDb")

  }
}
