package ord.zsd.ts2.omdbapi

import ord.zsd.ts2.omdbapi.OMDbOp.FindResponse
import org.atnos.eff.|=

sealed trait FindType
case class ById(imdbId: String) extends FindType
case class MovieByTitle(title: String) extends FindType
case class SeriesByTitle(title: String) extends FindType
case class EpisodeByTitle(seriesTitle: String, season: Int, episode: Int) extends FindType

sealed trait MediaType
case object MovieType extends MediaType
case object SeriesType extends MediaType
case object EpisodeType extends MediaType

sealed trait PlotType
case object ShortPlot extends PlotType
case object FullPlot extends PlotType

sealed trait TypeSpecifics
case object MovieSpecifics extends TypeSpecifics
case class SeriesSpecifics(totalSeasons: Int) extends TypeSpecifics
case class EpisodeSpecifics(season: Int,
                            episode: Int,
                            seriesId: String) extends TypeSpecifics

case class MediaDetails(title: String,
                        imdbId: String,
                        mediaType: MediaType,
                        year: String,
                        rated: Option[String],
                        released: Option[String],
                        runtime: Option[String],
                        genre: Seq[String],
                        director: Seq[String],
                        writer: Seq[String],
                        actors: Seq[String],
                        plot: Option[String],
                        language: Option[String],
                        country: Option[String],
                        awards: Option[String],
                        poster: Option[String],
                        metascore: Option[String],
                        imdbRating: Option[String],
                        imdbVotes: Option[String],
                        typeSpecifics: TypeSpecifics
                       )

trait OMDbOp[A]
case class FindDetails(findType: FindType,
                       mediaType: Option[MediaType] = None,
                       year: Option[Int] = None,
                       plotType: PlotType = ShortPlot,
                       tomatoesRating: Boolean = false) extends OMDbOp[FindResponse]

object OMDbOp {
  type _omdbOp[T] = OMDbOp |= T

  type FindResponse = Either[String, MediaDetails]

}