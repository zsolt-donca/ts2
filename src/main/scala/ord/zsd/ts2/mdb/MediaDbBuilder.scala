package ord.zsd.ts2.mdb

import ord.zsd.ts2.files.MediaPath
import ord.zsd.ts2.utils.UnapplyUtils.SomeInt

object MediaDbBuilder {

  def buildEpisode(path: MediaPath): List[EpisodeMedia] = {

    val flags = "(?i)"
    val releaserPrefix = "(\\w+-)?"
    val title = "([\\w\\.]+)"
    val season = "S(\\d{2})?"
    val episode = "E(\\d{2})"
    val rest = "(.*)"

    val singleEpisode = s"$flags$title\\.$season$episode\\.$rest".r
    val doubleEpisode = s"$flags$title\\.$season$episode$episode\\.$rest".r
    val withPart = s"$flags$releaserPrefix$title\\.Part\\.(\\d)\\.$rest".r
    val withEpisodeOnly = s"$flags$releaserPrefix$title\\.$episode\\.$rest".r
    val concatenatedEpisode = s"$flags$releaserPrefix$title\\.(\\d+)(\\.$rest)?".r

    val seasonDashEpisode = s"$flags$title\\.$season-(\\d{2})(l|( \\(East Coast\\))|( \\(West Coast\\)))?".r
    val seasonDashDoubleEpisode = s"$flags$title\\.$season-(\\d{2}) & (\\d{2})".r
    val seasonTimesEpisode = s"$flags$title\\.(\\d+)x(\\d+)\\.(.*)".r
    val seasonSpaceEpisode = s"$flags(.+)$season$episode(.*)".r

    path.baseName match {
      case singleEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episodeInt), _) =>
        List(EpisodeMedia(normalizeTitle(titleDotted), seasonInt, episodeInt, None, path))

      case doubleEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episode1), SomeInt(episode2), _) =>
        List(episode1, episode2).map(episode => EpisodeMedia(normalizeTitle(titleDotted), seasonInt, episode, None, path))

      case withPart(_, titleDotted, SomeInt(partNumber), _) =>
        List(EpisodeMedia(normalizeTitle(titleDotted), season = 1, episode = partNumber, None, path))

      case withEpisodeOnly(_, titleDotted, SomeInt(episodeInt), _) =>
        List(EpisodeMedia(normalizeTitle(titleDotted), season = 1, episodeInt, None, path))

      case concatenatedEpisode(_, titleDotted, SomeInt(seasonAndEpisode), _*) =>
        List(EpisodeMedia(normalizeTitle(titleDotted), season = seasonAndEpisode / 100, episode = seasonAndEpisode % 100, None, path))

      case seasonDashEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episodeInt), _*) =>
        List(EpisodeMedia(normalizeTitle(titleDotted), seasonInt, episodeInt, None, path))

      case seasonDashDoubleEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episode1), SomeInt(episode2)) =>
        List(episode1, episode2).map(episode => EpisodeMedia(normalizeTitle(titleDotted), seasonInt, episode, None, path))

      case seasonTimesEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episodeInt), _*) =>
        List(EpisodeMedia(normalizeTitle(titleDotted), seasonInt, episodeInt, None, path))

      case seasonSpaceEpisode(titleStr, SomeInt(seasonInt), SomeInt(episodeInt), _*) =>
        List(EpisodeMedia(normalizeTitle(titleStr), seasonInt, episodeInt, None, path))

      case _ =>
        List()
    }
  }

  def isTargetedFile(entry: MediaPath): Boolean = isMediaFile(entry) && !isSample(entry)

  private def normalizeTitle(titleDotted: String): String = {
    titleDotted.split("[\\. ]").filter(_.nonEmpty).map(_.capitalize).mkString(" ")
  }

  private def isMediaFile(entry: MediaPath): Boolean = {
    entry.isFile && entry.extension.exists(ext => Set(".mkv", ".avi", ".mp4").contains(ext))
  }

  private def isSample(entry: MediaPath): Boolean = {
    entry.baseName.startsWith("sample-") || entry.baseName.endsWith(".sample")
  }
}
