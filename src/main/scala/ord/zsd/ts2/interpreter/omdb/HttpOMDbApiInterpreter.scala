package ord.zsd.ts2.interpreter.omdb

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import cats.{Later, ~>}
import monix.eval.Task
import ord.zsd.ts2.Ts2System
import ord.zsd.ts2.omdbapi.OMDbOp.FindResponse
import ord.zsd.ts2.omdbapi._
import OMDbApiJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.atnos.eff.Async
import org.atnos.eff._
import all._
import org.atnos.eff.syntax.all._

import scala.collection.immutable.Seq
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object HttpOMDbApiInterpreter {

  import Ts2System._

  private val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = Http().outgoingConnection("www.omdbapi.com")

  // could be shortened as a Lambda with kind projector
  def interpret: OMDbOp ~> Future = new (OMDbOp ~> Future) {
    override def apply[A](fa: OMDbOp[A]): Future[A] = {
      fa match {
        case findMedia: FindDetails =>

          val httpRequest = Get(Uri("/").withQuery(buildQuery(findMedia)))
            .withHeaders(Accept(`application/json`))

          val findResponseFuture: Future[FindResponse] = Source.single(httpRequest)
            .via(connectionFlow)
            .runWith(Sink.head)
            .flatMap(httpResponse => Unmarshal(httpResponse).to[FindResponse])

          findResponseFuture
      }
    }
  }

  private def buildQuery(findMedia: FindDetails): Query = {
    val params: Seq[Traversable[(String, String)]] = List(
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

    Query(params.flatten.toMap)
  }

}
