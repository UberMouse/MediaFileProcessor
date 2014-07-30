package nz.ubermouse.anime.ds

import java.io.File
import java.nio.file.Files

class FileSystem extends TFileSystem {
  def getFilesIn(directory: File): Iterable[FileSystemObject] = {
    val files = getFilesRecursively(directory)
    files.map(f => FileSystemObject(f, f.getName))
  }

  private def getFilesRecursively(directory: File): Iterable[File] = {
    val filesInclDirs = directory.listFiles().toList
    val files = filesInclDirs.filter(_.isFile)
    val dirs = filesInclDirs.filter(_.isDirectory)

    files ++ dirs.map(getFilesRecursively).flatten
  }
}
