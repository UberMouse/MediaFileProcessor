package nz.ubermouse.anime

import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.{Episode, Series}

class TvdbMetaData(tvdb: TheTVDBApi) extends MetaData {
  implicit def seriesToSeriesMetaData(series: Series) = {
    SeriesMetaData(series.getSeriesName, series.getId)
  }
  implicit def episodeToEpisodeMetaData(episode: Episode) = {
    EpisodeMetaData(episode.getEpisodeName)
  }

  def search(seriesName: String): Option[SeriesMetaData] = {
    val results = tvdb.searchSeries(seriesName, "en")
    if(results.isEmpty) None else Some(results.get(0))
  }

  def forEpisode(seriesIdentifier: String, season: Int, episode: Int): Option[EpisodeMetaData] = {
    val result = tvdb.getEpisode(seriesIdentifier, season, episode, "en")
    if(result.getSeriesId != seriesIdentifier) None else Some(result)
  }


}
