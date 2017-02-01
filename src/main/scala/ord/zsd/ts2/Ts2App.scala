package ord.zsd.ts2

import java.nio.file.{StandardWatchEventKinds => EventType}

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.pattern.ask
import akka.util.Timeout
import better.files.File
import better.files.FileWatcher._
import fommil.sjs.FamilyFormats._
import ord.zsd.ts2.Ts2System._
import ord.zsd.ts2.app.Ts2AppActor
import ord.zsd.ts2.app.Ts2AppActor.{FolderChangedAction, MediaDbList, ReadAllMedia, SyncDb}
import ord.zsd.ts2.files.MediaPath
import ord.zsd.ts2.mdb.Media
import ord.zsd.ts2.seriesdb.{Added, FolderChanged, Removed}
import pureconfig.loadConfig

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object Ts2App extends App {

  val config = loadConfig[Config].get

  private val ts2AppActor = system.actorOf(Props[Ts2AppActor])

  ts2AppActor ! SyncDb(config.seriesFolder)

  private val watcher: ActorRef = File(config.seriesFolder).newWatcher(recursive = true)

  // watch for multiple events
  watcher ! when(events = EventType.ENTRY_CREATE, EventType.ENTRY_DELETE) {
    case (EventType.ENTRY_CREATE, file) => ts2AppActor ! FolderChangedAction(FolderChanged(MediaPath(file.pathAsString, file.isDirectory), Added))
    case (EventType.ENTRY_DELETE, file) => ts2AppActor ! FolderChangedAction(FolderChanged(MediaPath(file.pathAsString, isFolder = false), Removed))
  }

  private val route = {
    path("series") {
      get {
        complete(askForMediaDbList())
      }
    }
  }

  private val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  Runtime.getRuntime.addShutdownHook(new Thread(
    new Runnable {
      override def run(): Unit = {
        bindingFuture
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ => system.terminate()) // and shutdown when done
      }
    }
  ))

  private def askForMediaDbList(): Future[List[Media]] = {
    implicit val timeout = Timeout(30 seconds) // why do I need a timeout here? why can't the future be unbound?
    val future = ts2AppActor ? ReadAllMedia
    future.mapTo[MediaDbList].map(_.mediadList)
  }
}
