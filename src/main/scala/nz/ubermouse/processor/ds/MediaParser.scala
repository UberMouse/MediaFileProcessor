package nz.ubermouse.processor.ds

import java.io.File

case class MediaFile(name: String, season: Int, episode: Int, fso: FileSystemObject)

class MediaParser {
  def apply(files: Iterable[FileSystemObject], animeTitles: Iterable[String]): Iterable[MediaFile] = {
    files.map(fso => (fso, new FileNameParser(fso.name, animeTitles)))
         .filter{case(_, parser) => parser.isAnime}
         .map{case (fso, parser) => (fso, parser.anime.get)}
         .map{case(fso, pa) => MediaFile(pa.series, pa.season, pa.episode, fso)}
  }
}
