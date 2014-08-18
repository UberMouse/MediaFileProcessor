package nz.ubermouse.processor

import scaldi.Module
import com.omertron.thetvdbapi.TheTVDBApi
import nz.ubermouse.processor.ds._

class ProcessorModule extends Module {
  bind[TheTVDBApi] to new TheTVDBApi("CFBFA92BDDCC4F64")
  bind[MetaData] to injected[TvdbMetaData]
  bind[NameParser] to new RegexParser
  bind[MediaParser] to new MediaParser
  bind[FileProcessor] to injected[FileProcessor]
  bind[TFileSystem] to injected[FileSystem]
}
