package ord.zsd.ts2.omdbapi

import cats.free.Free

sealed trait OMDbApiAction[ResultType]

sealed trait FindType
case class FindById(imdbId: String) extends FindType
case class FindByTitle(title: String) extends FindType

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
                            seriesID: String) extends TypeSpecifics

sealed trait FindResponse extends Product
case class FindResult(title: String,
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
                     ) extends FindResponse
case class ErrorResult(message: String) extends FindResponse

case class FindMedia(findType: FindType,
                     mediaType: Option[MediaType] = None,
                     year: Option[Int] = None,
                     plotType: PlotType = ShortPlot,
                     tomatoesRating: Boolean = false) extends OMDbApiAction[FindResponse]

object FindMedia {

  type OMDbApiActionM[X] = Free[OMDbApiAction, X]

  def findMedia(findType: FindType,
                mediaType: Option[MediaType] = None,
                year: Option[Int] = None,
                plotType: PlotType = ShortPlot,
                tomatoesRating: Boolean = false): OMDbApiActionM[FindResponse] = Free.liftF(FindMedia(findType, mediaType, year, plotType, tomatoesRating))

}