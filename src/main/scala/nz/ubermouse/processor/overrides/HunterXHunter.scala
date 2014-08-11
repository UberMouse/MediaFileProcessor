package nz.ubermouse.processor.overrides

import nz.ubermouse.processor.ds.MediaFile
import nz.ubermouse.processor.SeriesMetaData

class HunterXHunter extends Override {
  def tvdbId: String = "252322"

  override def shouldPreProcess(animeFile: MediaFile): Boolean = {
    animeFile.name == "hunter x hunter"
  }

  def apply(animeFile: MediaFile, metaData: SeriesMetaData): MediaFile = {
    var af = animeFile
    if(af.episode > 58) {
      af = af.copy(episode = af.episode - 58, season = 2)
    }
    af.copy(name = af.name + " (2011)")
  }
}
