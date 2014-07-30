package nz.ubermouse.anime.overrides

import nz.ubermouse.anime.ds.AnimeFile
import nz.ubermouse.anime.SeriesMetaData

class SeasonOverride(overrideTo: String, overrideOn: String, newSeason: Int, id: Int) extends Override {
  def tvdbId: String = id.toString

  override def shouldPreProcess(animeFile: AnimeFile): Boolean = {
    if(animeFile.name.toLowerCase == overrideOn)
      return true
    false
  }

  def apply(animeFile: AnimeFile, metaData: SeriesMetaData = null): AnimeFile = {
    if(animeFile.name.toLowerCase == overrideOn)
      return animeFile.copy(name = overrideTo, season = newSeason)
    animeFile
  }
}
