package com.gravitydev.extras

import java.nio.file.{Files, Path, Paths, WatchEvent, StandardWatchEventKinds, WatchKey, WatchService}
import com.sun.nio.file.SensitivityWatchEventModifier
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

object DirectoryWatcher {
  sealed trait EventKind
  object EventKind {
    case object Modify extends EventKind
    case object Create extends EventKind
    case object Delete extends EventKind
  }
  
  final case class Change (path: Path, kind: EventKind)
  
  
  final case class Watch (private val ws: WatchService) {
    def close () = ws.close()
  }
  
  private def register (ws: WatchService, path: Path, keys: scala.collection.mutable.Map[WatchKey,Path]): Unit = {
    val key = path.register(
      ws,
      Array(
        StandardWatchEventKinds.ENTRY_CREATE, 
        StandardWatchEventKinds.ENTRY_MODIFY, 
        StandardWatchEventKinds.ENTRY_DELETE
      ).asInstanceOf[Array[WatchEvent.Kind[_]]],
      SensitivityWatchEventModifier.HIGH 
    )
    keys += key -> path
    
    for (f <- Files.newDirectoryStream(path).asScala if Files.isDirectory(f)) {
      register(ws, f, keys)
    }
    ()
  }
  
  def watch (
    path: Path, 
    listener: Change => Unit,
    onException: Throwable => Unit
  )(implicit ec: ExecutionContext): Watch = {
    val ws = path.getFileSystem.newWatchService
    
    ec.execute(new Runnable {
      def run {
        val keys = scala.collection.mutable.Map[WatchKey,Path]()
        
        register(ws, path, keys)
        
        try {
          while (!Thread.currentThread.isInterrupted) {
            var watchKey: WatchKey = null
            watchKey = ws.take
            for (ev <- watchKey.pollEvents.asScala) {
              val kind = ev.kind() match {
                case StandardWatchEventKinds.ENTRY_CREATE => EventKind.Create
                case StandardWatchEventKinds.ENTRY_MODIFY => EventKind.Modify
                case StandardWatchEventKinds.ENTRY_DELETE => EventKind.Delete
              }
              val ctx = ev.context.asInstanceOf[Path]
              val change = Change(keys(watchKey).resolve(ctx), kind)
              
              if (change.kind == EventKind.Create && Files.isDirectory(change.path)) {
                register(ws, change.path, keys)
              }
              
              listener(change)
            }
            watchKey.reset()
          }
        } catch {
          case e: Throwable => onException(e)
        } finally {
          ws.close()
        }
      }
    })
    Watch(ws)
  }
}
