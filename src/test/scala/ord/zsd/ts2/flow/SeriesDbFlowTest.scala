package ord.zsd.ts2.flow


import better.files.File
import ord.zsd.ts2.eff.future._
import ord.zsd.ts2.interpreter.mdb.StoreMediaDbInterpreter.MediaDb
import ord.zsd.ts2.mdb.{Media, MediaPath}
import org.atnos.eff._
import org.atnos.eff.syntax.all._
import org.scalatest.FunSuite

import scala.concurrent.duration._
import scala.language.postfixOps
import spray.json._
import fommil.sjs.FamilyFormats._
import ord.zsd.ts2.flow.SeriesDbFlow.{Added, FolderChangedEvent}
import SeriesDbFlow._
import ord.zsd.ts2.Ts2System._


class SeriesDbFlowTest extends FunSuite {
  val paths = List("american.horror.story.s01e02.repack.720p.bluray.x264-demand.mkv", "The.Night.Of.Part.6.1080i.HDTV.H.264.HUN-nIk.mkv")

  test("Simple stuff - in-memory db") {
    val res: Eff[Stack1, List[Media]] = SeriesDbFlow.run1(folderChangedEvents)

    val (list: List[List[Media]], mediaDb: MediaDb) = res.runEval.runList.runState(MediaDb.empty).runWriterUnsafe[String](println(_)).runFuture(10 seconds).run

    println(s"List: $list")
    println(s"Flat list: ${list.flatten}")
    println(s"MediaDb: $mediaDb")

    assert(list.size == 2)
    assert(list.flatten.size == 6)
    assert(mediaDb.entries.size == 4)
  }

  test("Simple stuff - disk-based db") {

    val jsonFile = File.newTemporaryFile(prefix = "ts2-test", suffix = "json")

    try {
      val res: Eff[Stack2, List[Media]] = SeriesDbFlow.run2(folderChangedEvents)(jsonFile)(MediaDb.empty)
      val list: List[List[Media]] = res.runEval.runList.runWriterUnsafe[String](println(_)).runFuture(10 seconds).run

      println(s"List: $list")
      println(s"Flat list: ${list.flatten}")

      assert(list.size == 2)
      assert(list.flatten.size == 6)

      assert(jsonFile.exists)
      val jsonStr = jsonFile.contentAsString
      println(s"Contents of file ${jsonFile.name}:\n$jsonStr")
      val mediaDb = jsonStr.parseJson.convertTo[MediaDb]
      assert(mediaDb.entries.size == 4)
    } finally {
      jsonFile.delete(swallowIOExceptions = true)
    }
  }

  def folderChangedEvents: List[FolderChangedEvent] = paths.map(path => FolderChangedEvent(MediaPath.file(path), Added))

}
