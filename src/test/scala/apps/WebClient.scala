package apps

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import ord.zsd.ts2.omdbapi.FindResponse
import ord.zsd.ts2.omdbapi.interpreter.OMDbApiJsonProtocol._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object WebClient {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] =
      Http().outgoingConnection("www.omdbapi.com")

    val responseFuture: Future[HttpResponse] =
      Source.single(Get(Uri("/")
        .withQuery(Query("t" -> "terminator")))
        .withHeaders(Accept(`application/json`))
      ).via(connectionFlow)
        .runWith(Sink.head)

    val findResponseFuture: Future[FindResponse] = responseFuture.flatMap(response => Unmarshal(response).to[FindResponse])

    findResponseFuture.andThen {
      case Success(response) => println(s"request succeded: $response")
      case Failure(_) => println("request failed")
    }.andThen {
      case _ => system.terminate()
    }
  }
}