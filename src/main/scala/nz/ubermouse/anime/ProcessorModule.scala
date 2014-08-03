package nz.ubermouse.anime

import scaldi.Module
import com.omertron.thetvdbapi.TheTVDBApi
import nz.ubermouse.anime.ds.DirectorySearcher

class ProcessorModule extends Module {
  bind [TheTVDBApi] to new TheTVDBApi("CFBFA92BDDCC4F64")
  bind [MetaData] to injected [TvdbMetaData]
  bind [AnimeProcessor] to injected [AnimeProcessor]
}
