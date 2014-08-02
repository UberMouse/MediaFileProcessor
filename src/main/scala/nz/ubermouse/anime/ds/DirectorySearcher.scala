package nz.ubermouse.anime.ds

import java.io.File

case class AnimeFile(name: String, season: Int, episode: Int, fso: FileSystemObject)

class DirectorySearcher(fs: TFileSystem = new FileSystem) {

  def apply(directory: File, animeTitles: Iterable[String]): Iterable[AnimeFile] = {
    val files = fs.getFilesIn(directory)
    files.map(fso => (fso, new FileNameParser(fso.name, animeTitles)))
         .filter{case(_, parser) => parser.isAnime}
         .map{case (fso, parser) => (fso, parser.anime.get)}
         .map{case(fso, pa) => AnimeFile(pa.series, pa.season, pa.episode, fso)}
  }

  def apply(path: String, animeTitles: Iterable[String]): Iterable[AnimeFile] = apply(new File(path), animeTitles)
}
