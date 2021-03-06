package ord.zsd.ts2.mdb

import ord.zsd.ts2.files.MediaPath
import ord.zsd.ts2.mdb._
import org.scalatest.FunSuite

import scala.io.Source

//noinspection OptionEqualsSome
class MediaDbBuilderTest extends FunSuite {
  test("Common video formats are targeted, except for samples") {
    import MediaDbBuilder.isTargetedFile

    assert(isTargetedFile(MediaPath.file("/mnt/ext_hdd/Media/Sorozatok/Bones/Bones.S11E01.HDTV.x264-KILLERS/Bones.S11E01.HDTV.x264-KILLERS.mp4")))
    assert(isTargetedFile(MediaPath.file("/mnt/ext_hdd/Media/Sorozatok/New Girl/New.Girl.S06E02.720p.HDTV.x264-AVS/New.Girl.S06E02.720p.HDTV.x264-AVS.mkv")))
    assert(isTargetedFile(MediaPath.file("./Boardwalk Empire/Boardwalk.Empire.S05E01.HDTV.XviD-AFG.avi")))

    assert(!isTargetedFile(MediaPath.file("./Bones/Bones.S11E01.HDTV.x264-KILLERS/Sample/sample-bones.s11e01.hdtv.x264-killers.mp4")))
    assert(!isTargetedFile(MediaPath.file("./2 Broke Girls/2.Broke.Girls.S06E10.1080p.HDTV.X264-DIMENSION/Sample/2.broke.girls.610.1080-dimension.sample.mkv")))
    assert(isTargetedFile(MediaPath.file("/mnt/ext_hdd/Media/Sorozatok/Sample Series/Sample.Series.S01E01.720p.HDTV.x264-AVS.mkv")))
  }

  test("Other files and folders are never targeted") {
    import MediaDbBuilder.isTargetedFile

    // simple folders or ones that disguise themselves as video files are not targeted
    assert(!isTargetedFile(MediaPath.folder("/mnt/ext_hdd/Media/Sorozatok")))
    assert(!isTargetedFile(MediaPath.folder("/mnt/ext_hdd/Media/Sorozatok/")))
    assert(!isTargetedFile(MediaPath.folder("/mnt/ext_hdd/Media/Sorozatok/Westworld/Westworld.S01.1080p.HDTV.x264-BATV")))
    assert(!isTargetedFile(MediaPath.folder("/mnt/ext_hdd/Media/Sorozatok/Anomaly.mkv")))

    // .nfo files are very common in torrents
    assert(!isTargetedFile(MediaPath.file("./Bones/Bones.S11E01.HDTV.x264-KILLERS/bones.s11e01.hdtv.x264-killers.nfo")))

    // .dat files are sometimes left over by torrent clients
    assert(!isTargetedFile(MediaPath.file("./Scorpion/~uTorrentPartFile_1998CAB3A.dat")))

    // there are sometimes subtitles
    assert(!isTargetedFile(MediaPath.file("./Mr Robot/Mr.Robot.S01.1080p.BluRay.x264-ROVERS/Subs/Mr.Robot.S01E08.1080p.BluRay.x264-ROVERS.sub")))
    assert(!isTargetedFile(MediaPath.file("./Mr Robot/Mr.Robot.S01.1080p.BluRay.x264-ROVERS/Subs/Mr.Robot.S01E09.1080p.BluRay.x264-ROVERS.idx")))
    assert(!isTargetedFile(MediaPath.file("./Wayward Pines/Wayward.Pines.S02.720p.HDTV.x264-MiXGROUP/Wayward.Pines.S02E01.720p.HDTV.x264-KILLERS/Wayward.Pines.S02E01.720p.HDTV.x264-KILLERS.srt")))
  }

  test("Video file names are properly interpreted") {

    assertBuildEpisode("./2 Broke Girls/2.Broke.Girls.S06E09.1080p.HDTV.X264-DIMENSION/2.Broke.Girls.S06E09.1080p.HDTV.X264-DIMENSION.mkv", "2 Broke Girls", 6, List(9))

    assertBuildEpisode("american.horror.story.s01e02.repack.720p.bluray.x264-demand.mkv", "American Horror Story", 1, List(2))
    assertBuildEpisode("insulin-band.of.brothers.e01.720p.mkv", "Band Of Brothers", 1, List(1))
    assertBuildEpisode("The.Shannara.Chronicles.S01E01E02.1080p.WEB-DL.H.264.HUN.ENG-nIk.mkv", "The Shannara Chronicles", 1, List(1, 2))
    assertBuildEpisode("scorpion.114.hdtv-lol.mp4", "Scorpion", 1, List(14))
    assertBuildEpisode("The.Night.Of.Part.6.1080i.HDTV.H.264.HUN-nIk.mkv", "The Night Of", 1, List(6))
    assertBuildEpisode("ER.S01-03.avi", "ER", 1, List(3))
    assertBuildEpisode("ER.S01-01 & 02.avi", "ER", 1, List(1, 2))
    assertBuildEpisode("ER.S03-12l.avi", "ER", 3, List(12))
    assertBuildEpisode("ER.S04-01 (East Coast).avi", "ER", 4, List(1))
    assertBuildEpisode("ER.S04-01 (West Coast).avi", "ER", 4, List(1))
    assertBuildEpisode("sln-surviving.jack.101.avi", "Surviving Jack", 1, List(1))
    assertBuildEpisode("The.Big.Bang.Theory.2x13.The.Friendship.Algorithm.720p.HDTV.x264-SXP.mkv", "The Big Bang Theory", 2, List(13))
    assertBuildEpisode("Vegtelen szerelem S01E24 WEBRip.mkv", "Vegtelen Szerelem", 1, List(24))
  }

  def assertBuildEpisode(path: String, title: String, season: Int, episodes: List[Int]): Unit = {
    val entry = MediaPath.file(path)
    assert(MediaDbBuilder.buildEpisode(entry) == episodes.map(episode => EpisodeMedia(title, season, episode, None, entry)))
  }

  test("Invalid file names are not parsed") {
    assert(MediaDbBuilder.buildEpisode(MediaPath.file("house md 101.avi")).isEmpty)
    assert(MediaDbBuilder.buildEpisode(MediaPath.file("house.md S01 E01 HDTV.avi")).isEmpty)
  }

  test("Targeted video file names should be properly interpreted") {
    val inputStream = getClass.getResourceAsStream("/mediadb/seriesFolder1.txt")
    val lines = try {
      Source.fromInputStream(inputStream).getLines().toVector.filter(_.nonEmpty)
    } finally {
      inputStream.close()
    }

    val episodeOptions = lines.map(line => MediaPath(line, isFolder = lines.exists(x => x != line && x.startsWith(line))))
      .filter(entry => MediaDbBuilder.isTargetedFile(entry))
      .map(entry => MediaDbBuilder.buildEpisode(entry))

    assert(episodeOptions.forall(_.nonEmpty))

    assert(lines.size == 1259)
    assert(episodeOptions.size == 663)
  }
}
