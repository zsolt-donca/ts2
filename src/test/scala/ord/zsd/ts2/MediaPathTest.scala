package ord.zsd.ts2

import ord.zsd.ts2.mdb.MediaPath
import org.scalatest.FunSuite

class MediaPathTest extends FunSuite {

  test("A folder doesn't have extension when its name doesn't contain dots") {
    val entry = MediaPath.folder("/mnt/ext_hdd/Media/Sorozatok/ER")
    assert(entry.name == "ER")
    assert(entry.baseName == "ER")
    assert(entry.extension.isEmpty)
    assert(!entry.isFile)
  }

  test("A folder doesn't have extension, even when it's name contains dots") {
    val entry = MediaPath.folder("/mnt/ext_hdd/Media/Sorozatok/Westworld/Westworld.S01.1080p.HDTV.x264-BATV")
    assert(entry.name == "Westworld.S01.1080p.HDTV.x264-BATV")
    assert(entry.baseName == "Westworld.S01.1080p.HDTV.x264-BATV")
    assert(entry.extension.isEmpty)
    assert(!entry.isFile)
  }

  test("A file doesn't have extension when its name doesn't contain dots") {
    val entry = MediaPath.file("/mnt/ext_hdd/Media/Sorozatok/Bones/Bones.S11E01.HDTV.x264-KILLERS/someFile")
    assert(entry.name == "someFile")
    assert(entry.baseName == "someFile")
    assert(entry.extension.isEmpty)
    assert(entry.isFile)
  }

  test("A file has an extension when its name contains dots") {
    val entry = MediaPath.file("/mnt/ext_hdd/Media/Sorozatok/Bones/Bones.S11E01.HDTV.x264-KILLERS/Bones.S11E01.HDTV.x264-KILLERS.mp4")
    assert(entry.name == "Bones.S11E01.HDTV.x264-KILLERS.mp4")
    assert(entry.baseName == "Bones.S11E01.HDTV.x264-KILLERS")
    assert(entry.extension.contains(".mp4"))
    assert(entry.isFile)
  }

  test("A file has all is parent folders and itself as path parts") {
    val entry = MediaPath.file("/mnt/ext_hdd/Media/Sorozatok/New Girl/New.Girl.S06E02.720p.HDTV.x264-AVS/New.Girl.S06E02.720p.HDTV.x264-AVS.mkv")
    assert(entry.pathParts == Vector(
      "", "mnt", "ext_hdd", "Media", "Sorozatok", "New Girl", "New.Girl.S06E02.720p.HDTV.x264-AVS", "New.Girl.S06E02.720p.HDTV.x264-AVS.mkv"
    ))
  }

  test("A path ending in a / has name") {
    val entry = MediaPath.folder("/mnt/ext_hdd/Media/Sorozatok/")
    assert(entry.name == "Sorozatok")
    assert(entry.baseName == "Sorozatok")
    assert(entry.extension.isEmpty)
  }
}
