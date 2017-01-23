package ord.zsd.ts2.mdb

import ord.zsd.ts2.utils.UnapplyUtils.SomeInt

object MediaDbBuilder {

  def buildEpisode(entry: DiskEntry): Option[Episode] = {

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

    entry.baseName match {
      case singleEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episodeInt), _) =>
        Some(Episode(normalizeTitle(titleDotted), seasonInt, SingleEpisode(episodeInt), entry))

      case doubleEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episode1), SomeInt(episode2), _) =>
        Some(Episode(normalizeTitle(titleDotted), seasonInt, DoubleEpisode(episode1, episode2), entry))

      case withPart(_, titleDotted, SomeInt(partNumber), _) =>
        Some(Episode(normalizeTitle(titleDotted), season = 1, SingleEpisode(partNumber), entry))

      case withEpisodeOnly(_, titleDotted, SomeInt(episodeInt), _) =>
        Some(Episode(normalizeTitle(titleDotted), season = 1, SingleEpisode(episodeInt), entry))

      case concatenatedEpisode(_, titleDotted, SomeInt(seasonAndEpisode), _*) =>
        Some(Episode(normalizeTitle(titleDotted), season = seasonAndEpisode / 100, SingleEpisode(seasonAndEpisode % 100), entry))

      case seasonDashEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episodeInt), _*) =>
        Some(Episode(normalizeTitle(titleDotted), seasonInt, SingleEpisode(episodeInt), entry))

      case seasonDashDoubleEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episode1), SomeInt(episode2)) =>
        Some(Episode(normalizeTitle(titleDotted), seasonInt, DoubleEpisode(episode1, episode2), entry))

      case seasonTimesEpisode(titleDotted, SomeInt(seasonInt), SomeInt(episodeInt), _*) =>
        Some(Episode(normalizeTitle(titleDotted), seasonInt, SingleEpisode(episodeInt), entry))

      case seasonSpaceEpisode(titleStr, SomeInt(seasonInt), SomeInt(episodeInt), _*) =>
        Some(Episode(normalizeTitle(titleStr), seasonInt, SingleEpisode(episodeInt), entry))

      case _ =>
        None
    }
  }

  def isTargetedFile(entry: DiskEntry): Boolean = isMediaFile(entry) && !isSample(entry)

  private def normalizeTitle(titleDotted: String): String = {
    titleDotted.split("[\\. ]").filter(_.nonEmpty).map(_.capitalize).mkString(" ")
  }

  private def isMediaFile(entry: DiskEntry): Boolean = {
    entry.isFile && entry.extension.exists(ext => Set(".mkv", ".avi", ".mp4").contains(ext))
  }

  private def isSample(entry: DiskEntry): Boolean = {
    entry.baseName.startsWith("sample-") || entry.baseName.endsWith(".sample")
  }
}
