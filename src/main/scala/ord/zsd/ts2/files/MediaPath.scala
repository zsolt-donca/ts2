package ord.zsd.ts2.files

case class MediaPath(path: String, isFolder: Boolean) {
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

object MediaPath {
  def file(path: String): MediaPath = MediaPath(path, isFolder = false)

  def folder(path: String): MediaPath = MediaPath(path, isFolder = true)
}