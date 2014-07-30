package nz.ubermouse.anime.ds

import java.io.File

case class FileSystemObject(path:File, name:String)

trait TFileSystem {
  def getFilesIn(path: String):Iterable[FileSystemObject] = getFilesIn(new File(path))
  def getFilesIn(path: File):Iterable[FileSystemObject]
}
