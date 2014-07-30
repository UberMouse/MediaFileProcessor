package nz.ubermouse.anime.ds

case class ParsedAnime(releaseGroup: String, series: String, season: Int, episode: Int)

class FileNameParser(fileName:String, animeTitles: Iterable[String]) {
  val RELEASER_R = "(?i:\\[([\\w-]+)\\].*"
  val SERIES_R = "($seriesname)"
  val SEASON_R = "[^s]+S?([0-9])?.*"
  val EPISODE_R = " e?p?\\.?([0-9]{2,4})(?:v[0-9])? .*(?:\\[|\\().*)"

  def isAnime = {
    anime.isDefined  
  }
  
  def anime = {
    val series = animeTitles.map(x => x.toLowerCase)

    series.map(s => {
      val regex = buildRegex(s)
      cleanName match {
        case regex(releaseGroup, seriesName, season, episode) => Some(ParsedAnime(releaseGroup, seriesName, if (season == null) 1 else season.toInt, episode.toInt))
        case _ => None
      }
    }).flatten.headOption
  }

  private def buildRegex(seriesName:String) = (RELEASER_R +
                                              SERIES_R.replaceFirst("\\$seriesname", seriesName) +
                                              SEASON_R +
                                              EPISODE_R).r("Release Group", "Series Name", "Season Number", "Episode Number", "remainder")

  private def cleanName = {
    fileName.toLowerCase.replaceAll("_", " ")
  }
}
