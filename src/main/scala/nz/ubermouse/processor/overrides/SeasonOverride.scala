package nz.ubermouse.processor.overrides

import nz.ubermouse.processor.ds.MediaFile
import nz.ubermouse.processor.SeriesMetaData

class SeasonOverride(overrideTo: String, overrideOn: String, newSeason: Int, id: Int) extends Override {
  def tvdbId: String = id.toString

  override def shouldPreProcess(animeFile: MediaFile): Boolean = {
    if(animeFile.name.toLowerCase == overrideOn)
      return true
    false
  }

  def apply(animeFile: MediaFile, metaData: SeriesMetaData = null): MediaFile = {
    if(animeFile.name.toLowerCase == overrideOn)
      return animeFile.copy(name = overrideTo, season = newSeason)
    animeFile
  }
}
