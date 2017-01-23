package ord.zsd.ts2.mdb

import java.net.URI

sealed trait EpisodePosition
case class SingleEpisode(episode: Int) extends EpisodePosition
case class DoubleEpisode(episode1: Int, episode2: Int) extends EpisodePosition

case class Episode(seriesTitle: String,
                   season: Int,
                   episode: EpisodePosition,
                   path: DiskEntry)

case class Details(imdbId: String,
                   title: String,
                   year: (Int, Option[Int]),
                   genre: Seq[String],
                   plot: String,
                   poster: URI,
                   imdbRating: Double,
                   totalSeasons: Option[Int])

case class Series(details: Details,
                  episodes: Seq[Episode])

case class MediaDb(series: Seq[Series])
