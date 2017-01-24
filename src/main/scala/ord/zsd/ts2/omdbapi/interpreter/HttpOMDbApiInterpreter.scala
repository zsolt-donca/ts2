package ord.zsd.ts2.omdbapi.interpreter

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import cats.~>
import monix.eval.Task
import ord.zsd.ts2.Ts2System
import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.omdbapi.interpreter.OMDbApiJsonProtocol._

import scala.collection.immutable.Seq

object HttpOMDbApiInterpreter {

  // could be shortened as a Lambda with kind projector
  def interpret: OMDbApiAction ~> Task = new (OMDbApiAction ~> Task) {
    override def apply[A](fa: OMDbApiAction[A]): Task[A] = {
      fa match {
        case findMedia: FindMedia =>

          import Ts2System._

          val connectionFlow = Http().outgoingConnection("www.omdbapi.com")

          val httpRequest = Get(Uri("/").withQuery(buildQuery(findMedia)))
            .withHeaders(Accept(`application/json`))

          lazy val findResponseFuture = Source.single(httpRequest)
            .via(connectionFlow)
            .runWith(Sink.head)
            .flatMap(httpResponse => Unmarshal(httpResponse).to[FindResponse])

          val task: Task[FindResponse] = Task.defer(Task.fromFuture(findResponseFuture))

          task.asInstanceOf[Task[A]]
      }
    }
  }

  private def buildQuery(findMedia: FindMedia): Query = {
    val params: Seq[Option[(String, String)]] = List(
      Some(findMedia.findType match {
        case FindById(imdbId) => "i" -> imdbId
        case FindByTitle(title) => "t" -> title
      }),
      findMedia.mediaType.map({
        case MovieType => "type" -> "movie"
        case SeriesType => "type" -> "series"
        case EpisodeType => "type" -> "episode"
      }),
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
