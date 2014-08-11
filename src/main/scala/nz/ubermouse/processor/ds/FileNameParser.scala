package nz.ubermouse.processor.ds

case class ParsedMediaFile(releaseGroup: String, series: String, season: Int, episode: Int)

class FileNameParser(fileName:String, titles: Iterable[String]) {


  def isAnime = {
    anime.isDefined  
  }
  
  def anime = {
    val series = titles.map(x => x.toLowerCase)

    series.map(s => {
      val parser = new NameParser(series)
      parser.parse(fileName) match {
        case media: Some[ParsedMediaFile] => media
        case _ => None
      }
    }).flatten.headOption
  }
}
