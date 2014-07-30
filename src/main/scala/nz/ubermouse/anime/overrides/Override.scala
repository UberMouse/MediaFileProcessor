package nz.ubermouse.anime.overrides

import nz.ubermouse.anime.ds.AnimeFile
import com.omertron.thetvdbapi.model.Series
import nz.ubermouse.anime.SeriesMetaData

trait Override {
  def tvdbId: String
  def shouldPreProcess(animeFile: AnimeFile): Boolean = false
  def apply(animeFile: AnimeFile, metaData: SeriesMetaData = null): AnimeFile
}
