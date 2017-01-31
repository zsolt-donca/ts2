package ord.zsd.ts2.flow


import better.files.File
import fommil.sjs.FamilyFormats._
import ord.zsd.ts2.Ts2System._
import ord.zsd.ts2.eff.future._
import ord.zsd.ts2.flow.SeriesDbFlow._
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter.MediaDb
import ord.zsd.ts2.mdb.MediaPath
import ord.zsd.ts2.seriesdb.{Added, FolderChanged}
import org.atnos.eff._
import org.atnos.eff.syntax.all._
import org.scalatest.FunSuite
import spray.json._

import scala.concurrent.duration._
import scala.language.postfixOps


class SeriesDbFlowTest extends FunSuite {
  test("Simple stuff - in-memory db") {
    val res: Eff[Stack1, Unit] = SeriesDbFlow.run1(folderChangedEvent)

    val (_, mediaDb: MediaDb) = res.runEval.runList.runState(MediaDb.empty).runWriterUnsafe[String](println(_)).runFuture(10 seconds).run

    println(s"MediaDb: $mediaDb")
    assert(mediaDb.entries.size == 2)
  }

  test("Simple stuff - disk-based db") {

    val jsonFile = File.newTemporaryFile(prefix = "ts2-test", suffix = "json")

    try {
      val res: Eff[Stack2, Unit] = SeriesDbFlow.run2(folderChangedEvent)(jsonFile)(MediaDb.empty)

      res.runEval.runList.runWriterUnsafe[String](println(_)).runFuture(10 seconds).run

      assert(jsonFile.exists)
      val jsonStr = jsonFile.contentAsString
      println(s"Contents of file ${jsonFile.name}:\n$jsonStr")
      val mediaDb = jsonStr.parseJson.convertTo[MediaDb]
      assert(mediaDb.entries.size == 2)
    } finally {
      jsonFile.delete(swallowIOExceptions = true)
    }
  }

  def folderChangedEvent = FolderChanged(MediaPath.file("american.horror.story.s01e02.repack.720p.bluray.x264-demand.mkv"), Added)

}
