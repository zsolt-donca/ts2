package ord.zsd.ts2

import java.nio.file.{StandardWatchEventKinds => EventType}

import akka.actor.{ActorRef, Props}
import better.files.FileWatcher._
import better.files._
import pureconfig.loadConfig
import Ts2System._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes.`text/html(UTF-8)`
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives.{complete, get, path}
import ord.zsd.ts2.app.Ts2AppActor
import ord.zsd.ts2.app.Ts2AppActor.{FolderChangedAction, SyncDb}
import ord.zsd.ts2.files.MediaPath
import ord.zsd.ts2.seriesdb.{Added, FolderChanged, Removed}

import scala.concurrent.Future

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

  private val route =
    path("hello") {
      get {
        complete(Future(HttpEntity(`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")))
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
}
