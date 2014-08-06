package nz.ubermouse.processor.overrides

import nz.ubermouse.processor.ds.MediaFile
import com.omertron.thetvdbapi.model.Series
import nz.ubermouse.processor.SeriesMetaData

class Chuunibyou extends Override {
  val S2Name = "Chuunibyou demo Koi ga Shitai! Ren"

  def tvdbId: String = "261862"
  def apply(animeFile: MediaFile, metaData: SeriesMetaData = null): MediaFile = {
    if(animeFile.name.toLowerCase == S2Name.toLowerCase)
      return animeFile.copy(season = 2)
    animeFile
  }
}
