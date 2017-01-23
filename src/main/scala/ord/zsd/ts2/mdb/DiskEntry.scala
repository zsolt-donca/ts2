package ord.zsd.ts2.mdb

import cats.{Eq, Show}

case class DiskEntry(path: String, isFolder: Boolean) {
  val pathParts: Seq[String] = path.split('/').toVector

  val name: String = pathParts.lastOption.getOrElse("")

  val (baseName: String, extension: Option[String]) = {
    (isFile, name.lastIndexOf('.')) match {
      case (true, dotPos) if dotPos >= 0 =>
        val (baseName, ext) = name.splitAt(dotPos)
        (baseName, Some(ext))
      case _ =>
        (name, None)
    }
  }

  def isFile: Boolean = !isFolder
}

object DiskEntry {

  def file(path: String): DiskEntry = DiskEntry(path, isFolder = false)

  def folder(path: String): DiskEntry = DiskEntry(path, isFolder = true)

  implicit val show: Show[DiskEntry] = Show.fromToString[DiskEntry]

  implicit val eq: Eq[DiskEntry] = Eq.fromUniversalEquals[DiskEntry]
}