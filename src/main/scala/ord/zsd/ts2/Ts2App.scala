package ord.zsd.ts2

import java.nio.file.{StandardWatchEventKinds => EventType}

import akka.actor.{ActorRef, Props}
import better.files.FileWatcher._
import better.files._
import pureconfig.loadConfig
import Ts2System._
import ord.zsd.ts2.app.Ts2AppActor
import ord.zsd.ts2.app.Ts2AppActor.{FolderChangedAction, SyncDb}
import ord.zsd.ts2.files.MediaPath
import ord.zsd.ts2.seriesdb.{Added, FolderChanged, Removed}

object Ts2App extends App {

  val config = loadConfig[Config].get

  private val watcher: ActorRef = File(config.seriesFolder).newWatcher(recursive = true)

  private val ts2AppActor = system.actorOf(Props[Ts2AppActor])

  ts2AppActor ! SyncDb(config.seriesFolder)

  // watch for multiple events
  watcher ! when(events = EventType.ENTRY_CREATE, EventType.ENTRY_DELETE) {
    case (EventType.ENTRY_CREATE, file) => ts2AppActor ! FolderChangedAction(FolderChanged(MediaPath(file.pathAsString, file.isDirectory), Added))
    case (EventType.ENTRY_DELETE, file) => ts2AppActor ! FolderChangedAction(FolderChanged(MediaPath(file.pathAsString, isFolder = false), Removed))
  }

  Runtime.getRuntime.addShutdownHook(new Thread(() => {
      system.terminate()
  }))
}
