package ord.zsd.ts2.interpreter.omdb

import cats.~>
import ord.zsd.ts2.Ts2System
import ord.zsd.ts2.interpreter.omdb.OMDbApiJsonProtocol._
import ord.zsd.ts2.omdbapi.OMDbOp.FindResponse
import ord.zsd.ts2.omdbapi._
import spray.json._

import scala.concurrent.Future
import scala.language.postfixOps
import scalaj.http.{Http, HttpResponse}

object HttpOMDbApiInterpreter {

  import Ts2System._

  System.setProperty("http.keepAlive", "true")
  System.setProperty("http.maxConnections", "20")

  // could be shortened as a Lambda with kind projector
  def interpret: OMDbOp ~> Future = new (OMDbOp ~> Future) {
    override def apply[A](fa: OMDbOp[A]): Future[A] = {
      fa match {
        case findMedia: FindDetails =>

          Future {
            val response: HttpResponse[String] = Http("http://www.omdbapi.com").params(buildQuery(findMedia)).asString
            val findResponse = response.body.parseJson.convertTo[FindResponse]

            findResponse
          }.asInstanceOf[Future[A]]
      }
    }
  }

  private def buildQuery(findMedia: FindDetails): List[(String, String)] = {
    val params: List[Traversable[(String, String)]] = List(
      findMedia.findType match {
        case ById(imdbId) =>
          List("i" -> imdbId)
        case MovieByTitle(title) =>
          List("t" -> title, "type" -> "movie")
        case SeriesByTitle(title) =>
          List("t" -> title, "type" -> "series")
        case EpisodeByTitle(seriesTitle, season, episode) =>
          List("t" -> seriesTitle, "type" -> "episode", "season" -> season.toString, "episode" -> episode.toString)
      },
      findMedia.year.map(year => "y" -> year.toString),
      Some(findMedia.plotType match {
        case ShortPlot => "plot" -> "short"
        case FullPlot => "plot" -> "full"
      }),
      Some("r" -> "json"),
      Some("tomatoes" -> findMedia.tomatoesRating.toString)
    )

    params.flatten
  }

}
