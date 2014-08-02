package nz.ubermouse.anime

import scala.collection.immutable.HashMap
import com.omertron.thetvdbapi.model.Series
import nz.ubermouse.anime.ds.AnimeFile
import nz.ubermouse.anime.overrides._
import nz.ubermouse.anime.SeriesMetaData
import scala.Some
import nz.ubermouse.anime.ds.AnimeFile

object Overrides {
  private var overrides = new HashMap[String, Override]

  def register(ovrride: Override) {
    overrides += ((ovrride.tvdbId, ovrride))
  }

  def apply(anime: AnimeFile, metaData: SeriesMetaData) = {
    overrides.get(metaData.id).flatMap(x => Some(x(anime, metaData))).getOrElse(anime)
  }

  def getPreProcessorForFile(animeFile: AnimeFile): Option[Override] = {
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
