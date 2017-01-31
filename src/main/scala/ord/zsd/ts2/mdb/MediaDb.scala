package ord.zsd.ts2.mdb

import ord.zsd.ts2.omdbapi.{MediaDetails, MediaType}
import org.atnos.eff.|=

sealed trait Media

case class SeriesMedia(title: String,
                       details: Option[MediaDetails]) extends Media

case class EpisodeMedia(seriesTitle: String,
                        season: Int,
                        episode: Int,
                        details: Option[MediaDetails],
                        path: MediaPath) extends Media

case class MovieMedia(title: String,
                      details: Option[MediaDetails],
                      path: MediaPath) extends Media

trait MediaDbOp[A]
case class SaveEntry(mediaCollectionEntry: Media) extends MediaDbOp[Long]
case class DeleteEntry(id: Long) extends MediaDbOp[Boolean]

case class FindEntryById(id: Long) extends MediaDbOp[Option[Media]]
case class FindEntryByTitle(title: String, mediaType: MediaType) extends MediaDbOp[Option[Media]]
case class FindAllEntries() extends MediaDbOp[List[Media]]

case class DeleteEntriesByPath(path: MediaPath) extends MediaDbOp[Int]

object MediaDbOp {
  type _mediaDbOp[T] = MediaDbOp |= T
}
