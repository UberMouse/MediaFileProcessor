package nz.ubermouse.processor.ds

class RegexParser extends NameParser {
  val RELEASER_R = "(?i:\\[([\\w-]+)\\].*"
  val SERIES_R = "($seriesname)"
  val SEASON_R = "[^s]+S?([0-9])?.*"
  val EPISODE_R = " e?p?\\.?([0-9]{2,4})(?:v[0-9])? .*(?:\\[|\\().*)"
  val HORRIBLE_SUBS = "\\[(horriblesubs)\\] ($seriesname) - ()([0-9]{1,3}).*"
  val LOOSE = "\\[(.+)\\] ($seriesname) ()([0-9]{1,4}) .*"

  def parse(name: String, series: Iterable[String]): Option[ParsedMediaFile] = {
    val regexes = buildRegex(series)
    for(r <- regexes) {
      val matcher = r.pattern.matcher(name)
      if(matcher.matches()) {
        val releaseGroup = matcher.group(1)
        val seriesName = matcher.group(2)
        val season = matcher.group(3)
        val episode = matcher.group(4)

        return Option(ParsedMediaFile(releaseGroup,
                                      seriesName,
                                      if(season == null || season.isEmpty) 1 else season.toInt,
                                      episode.dropWhile(_ == '0').toInt))
      }
    }
    None
  }

  private def buildRegex(names: Iterable[String]) = {
    val generic = names.map(seriesName => (RELEASER_R +
      SERIES_R.replaceFirst("\\$seriesname", seriesName) +
      SEASON_R +
      EPISODE_R).r("Release Group", "Series Name", "Season Number", "Episode Number", "remainder"))
    generic ++
    names.map(seriesName => HORRIBLE_SUBS.replaceFirst("\\$seriesname", seriesName).r("Release Group", "Series Name", "Season Number", "Episode Number")) ++
    names.map(seriesName => LOOSE.replaceFirst("\\$seriesname", seriesName).r)
  }
}
