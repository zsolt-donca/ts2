package ord.zsd.ts2.interpreter.mdb

import ord.zsd.ts2.files.MediaPath
import ord.zsd.ts2.mdb._
import ord.zsd.ts2.omdbapi.{EpisodeType, MediaType, MovieType, SeriesType}
import ord.zsd.ts2.store.StoreOp
import ord.zsd.ts2.store.StoreOp.{readStore, writeStore}
import org.atnos.eff._

/*_*/
object StoreMediaDbInterpreter {

  case class MediaDbEntry(id: Long, media: Media)
  case class MediaDb(entries: Vector[MediaDbEntry], nextId: Long)
  object MediaDb {
    def empty: MediaDb = MediaDb(Vector.empty, 0)
  }

  type MediaDbStore[A] = StoreOp[MediaDb, A]
  type _mediaDbStore[R] = MediaDbStore |= R

  def translate[R, U, A](e: Eff[R, A])(implicit member: Member.Aux[MediaDbOp, R, U],
                                       mediaDbStore: _mediaDbStore[U]): Eff[U, A] = {
    interpret.translate(e)(trans)
  }

  private def trans[R: _mediaDbStore]: Translate[MediaDbOp, R] = new Translate[MediaDbOp, R] {
    override def apply[X](kv: MediaDbOp[X]): Eff[R, X] = kv match {
      case SaveEntry(media) => saveEntry(media)
      case DeleteEntry(id) => deleteEntry(id)
      case FindEntryById(id) => findEntryById(id)
      case FindEntryByTitle(title, mediaType) => findEntryByTitle(title, mediaType)
      case FindAllEntries() => findAllEntries()
      case DeleteEntriesByPath(path) => deleteEntriesByPath(path)
    }
  }

  private def saveEntry[R: _mediaDbStore](media: Media): Eff[R, Long] = {
    for {
      mediaDb <- readStore[MediaDb, R]
      MediaDb(entries, nextId) = mediaDb
      _ <- writeStore[MediaDb, R](MediaDb(entries :+ MediaDbEntry(nextId, media), nextId + 1))
    } yield nextId
  }

  private def deleteEntry[R: _mediaDbStore](id: Long): Eff[R, Boolean] = {
    for {
      mediaDb <- readStore[MediaDb, R]
      MediaDb(entries, nextId) = mediaDb
      newEntries = entries.filter(_.id != id)
      _ <- writeStore[MediaDb, R](MediaDb(newEntries, nextId))
    } yield entries != newEntries
  }

  private def findEntryById[R: _mediaDbStore](id: Long): Eff[R, Option[Media]] = {
    readStore[MediaDb, R].map(mediaDb => mediaDb.entries.find(_.id == id).map(_.media))
  }

  private def findEntryByTitle[R: _mediaDbStore](title: String, mediaType: MediaType): Eff[R, Option[Media]] = {
    readStore[MediaDb, R].map(mediaDb => mediaDb.entries.map(entry => (entry.media, mediaType)).collectFirst({
      case (media@SeriesMedia(`title`, _), SeriesType) => media
      case (media@EpisodeMedia(`title`, _, _, _, _), EpisodeType) => media
      case (media@MovieMedia(`title`, _, _), MovieType) => media
    }))
  }

  private def findAllEntries[R: _mediaDbStore](): Eff[R, List[Media]] = {
    readStore[MediaDb, R].map(mediaDb => mediaDb.entries.map(_.media).toList)
  }

  private def deleteEntriesByPath[R: _mediaDbStore](path: MediaPath): Eff[R, Int] = {
    for {
      mediaDb <- readStore[MediaDb, R]
      MediaDb(entries, nextId) = mediaDb
      newEntries = entries.filterNot(entry => entry.media match {
        case EpisodeMedia(_, _, _, _, `path`) => true
        case MovieMedia(_, _, `path`) => true
        case _ => false
      })
      _ <- writeStore[MediaDb, R](MediaDb(newEntries, nextId))
    } yield entries.size - newEntries.size
  }
}
