package nz.ubermouse.processor

import scala.collection.immutable.HashMap
import com.omertron.thetvdbapi.model.Series
import nz.ubermouse.processor.ds.MediaFile
import nz.ubermouse.processor.overrides._
import nz.ubermouse.processor.SeriesMetaData
import scala.Some
import nz.ubermouse.processor.ds.MediaFile

object Overrides {
  private var overrides = new HashMap[String, Override]

  def register(ovrride: Override) {
    overrides += ((ovrride.tvdbId, ovrride))
  }

  def apply(anime: MediaFile, metaData: SeriesMetaData) = {
    overrides.get(metaData.id).flatMap(x => Some(x(anime, metaData))).getOrElse(anime)
  }

  def getPreProcessorForFile(animeFile: MediaFile): Option[Override] = {
    for((id, ovrride) <- overrides) {
      if(ovrride.shouldPreProcess(animeFile))
        return Some(ovrride)
    }
    None
  }

  // todo make overrides auto register
  register(new Chuunibyou)
  register(new Hamatora)
  register(new SwordArtOnline)
  register(new SpaceDandy)
}
