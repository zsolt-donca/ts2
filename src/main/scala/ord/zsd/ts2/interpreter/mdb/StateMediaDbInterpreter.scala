package ord.zsd.ts2.interpreter.mdb

import cats._
import cats.data.State
import cats.data.State._
import ord.zsd.ts2.mdb._
import ord.zsd.ts2.omdbapi.{EpisodeType, MediaType, MovieType, SeriesType}
import org.atnos.eff._

object StateMediaDbInterpreter {

  case class MediaDbEntry(id: Long, media: Media)
  case class MediaDb(entries: Vector[MediaDbEntry], nextId: Long)

  type MediaDbState[A] = State[MediaDb, A]
  type _mediaDbState[R] = MediaDbState |= R

  def interpret: MediaDbOp ~> MediaDbState = {
    new ~>[MediaDbOp, MediaDbState] {
      override def apply[A](fa: MediaDbOp[A]): MediaDbState[A] = fa match {
        case SaveEntry(media) => saveEntry(media).asInstanceOf[MediaDbState[A]]
        case DeleteEntry(id) => deleteEntry(id).asInstanceOf[MediaDbState[A]]
        case FindEntryById(id) => findEntryById(id).asInstanceOf[MediaDbState[A]]
        case FindEntryByTitle(title, mediaType) => findEntryByTitle(title, mediaType).asInstanceOf[MediaDbState[A]]
        case FindAllEntries() => findAllEntries().asInstanceOf[MediaDbState[A]]
        case DeleteEntriesByPath(path) => deleteEntriesByPath(path).asInstanceOf[MediaDbState[A]]
      }
    }
  }

  def saveEntry(media: Media): MediaDbState[Long] = {
    for {
      mediaDb <- get[MediaDb]
      MediaDb(entries, nextId) = mediaDb
      _ <- set[MediaDb](MediaDb(entries :+ MediaDbEntry(nextId, media), nextId + 1))
    } yield nextId
  }

  def deleteEntry(id: Long): MediaDbState[Boolean] = {
    for {
      mediaDb <- get[MediaDb]
      MediaDb(entries, nextId) = mediaDb
      newEntries = entries.filter(_.id != id)
      _ <- set[MediaDb](MediaDb(newEntries, nextId))
    } yield entries != newEntries
  }

  def findEntryById(id: Long): MediaDbState[Option[Media]] = {
    get.map(mediaDb => mediaDb.entries.find(_.id == id).map(_.media))
  }

  def findEntryByTitle(title: String, mediaType: MediaType): MediaDbState[Option[Media]] = {
    get[MediaDb].map(mediaDb => mediaDb.entries.map(entry => (entry.media, mediaType)).collectFirst({
      case (media@SeriesMedia(`title`, _), SeriesType) => media
      case (media@EpisodeMedia(`title`, _, _, _, _), EpisodeType) => media
      case (media@MovieMedia(`title`, _, _), MovieType) => media
    }))
  }

  def findAllEntries(): MediaDbState[List[Media]] = {
    get[MediaDb].map(mediaDb => mediaDb.entries.map(_.media).toList)
  }

  def deleteEntriesByPath(path: MediaPath): MediaDbState[Int] = {
    for {
      mediaDb <- get[MediaDb]
      MediaDb(entries, nextId) = mediaDb
      newEntries = entries.filterNot(entry => entry.media match {
        case EpisodeMedia(_, _, _, _, `path`) => true
        case MovieMedia(_, _, `path`) => true
        case _ => false
      })
      _ <- set(MediaDb(newEntries, nextId))
    } yield entries.size - newEntries.size
  }
}
