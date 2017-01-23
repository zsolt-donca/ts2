package ord.zsd.ts2.utils

object UnapplyUtils {

  object SomeInt {
    def unapply(s: String): Option[Int] = {
      try {
        Some(s.toInt)
      } catch {
        case _: NumberFormatException => None
      }
    }
  }

}
