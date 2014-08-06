package nz.ubermouse.processor

import scaldi.Module
import com.omertron.thetvdbapi.TheTVDBApi
import nz.ubermouse.processor.ds.{FileSystem, TFileSystem, MediaParser}

class ProcessorModule extends Module {
  bind[TheTVDBApi] to new TheTVDBApi("CFBFA92BDDCC4F64")
  bind[MetaData] to injected[TvdbMetaData]
  bind[MediaParser] to new MediaParser
  bind[FileProcessor] to injected[FileProcessor]
  bind[TFileSystem] to injected[FileSystem]
}
