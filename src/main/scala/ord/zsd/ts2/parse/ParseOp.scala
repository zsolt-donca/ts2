package ord.zsd.ts2.parse

import ord.zsd.ts2.mdb.{EpisodeMedia, MediaPath, MovieMedia}
import org.atnos.eff.|=

trait ParseOp[A]
case class ParseSeries(path: MediaPath) extends ParseOp[List[EpisodeMedia]]
case class ParseMovie(path: MediaPath) extends ParseOp[List[MovieMedia]]

object ParseOp {
  type _parseOp[T] = ParseOp |= T

}
