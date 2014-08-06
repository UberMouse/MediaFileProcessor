package nz.ubermouse.processor.overrides

import nz.ubermouse.processor.ds.MediaFile
import com.omertron.thetvdbapi.model.Series
import nz.ubermouse.processor.SeriesMetaData

trait Override {
  def tvdbId: String
  def shouldPreProcess(animeFile: MediaFile): Boolean = false
  def apply(animeFile: MediaFile, metaData: SeriesMetaData = null): MediaFile
}
