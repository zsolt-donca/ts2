package ord.zsd.ts2.parse

import ord.zsd.ts2.files.MediaPath
import ord.zsd.ts2.mdb.{EpisodeMedia, MovieMedia}
import org.atnos.eff.|=

sealed trait ParseOp[A]
case class ParseSeries(path: MediaPath) extends ParseOp[List[EpisodeMedia]]
case class ParseMovie(path: MediaPath) extends ParseOp[List[MovieMedia]]

object ParseOp {
  type _parseOp[T] = ParseOp |= T

}
