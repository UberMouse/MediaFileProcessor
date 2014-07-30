package nz.ubermouse.anime.overrides

import nz.ubermouse.anime.ds.AnimeFile
import com.omertron.thetvdbapi.model.Series
import nz.ubermouse.anime.SeriesMetaData

class Chuunibyou extends Override {
  val S2Name = "Chuunibyou demo Koi ga Shitai! Ren"

  def tvdbId: String = "261862"
  def apply(animeFile: AnimeFile, metaData: SeriesMetaData = null): AnimeFile = {
    if(animeFile.name.toLowerCase == S2Name.toLowerCase)
      return animeFile.copy(season = 2)
    animeFile
  }
}
