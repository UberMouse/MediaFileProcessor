package nz.ubermouse.anime

case class SeriesMetaData(name: String,
                          id: String)
case class EpisodeMetaData(name: String)

abstract class MetaData {
  def search(seriesName: String): Option[SeriesMetaData]
  def forEpisode(seriesIdentifier: String, season: Int, episode: Int): Option[EpisodeMetaData]
}
