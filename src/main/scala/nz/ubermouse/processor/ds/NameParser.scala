package nz.ubermouse.processor.ds

import nz.ubermouse.processor.ds.{ParsedMediaFile, MediaFile, FileSystemObject}

trait NameParser {
  def parse(name: String, titles: Iterable[String]): Option[ParsedMediaFile]
}
