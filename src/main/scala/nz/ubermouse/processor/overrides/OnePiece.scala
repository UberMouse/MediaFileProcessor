package nz.ubermouse.processor.overrides

import nz.ubermouse.processor.ds.MediaFile
import nz.ubermouse.processor.SeriesMetaData

class OnePiece extends Override {
  def tvdbId: String = "81797"

  override def shouldPreProcess(animeFile: MediaFile): Boolean = {
    animeFile.name == "one piece"
  }

  def apply(animeFile: MediaFile, metaData: SeriesMetaData): MediaFile = {
    var af = animeFile
    if(af.episode > 30) {
      af = af.copy(episode = af.episode - 30, season = 3)
    }
    else if(af.episode > 8) {
      af = af.copy(episode = af.episode - 8, season = 2)
    }
    af
  }
}
