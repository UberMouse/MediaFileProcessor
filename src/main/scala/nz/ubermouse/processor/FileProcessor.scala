package nz.ubermouse.processor

import nz.ubermouse.processor.ds._
import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.Series
import java.nio.file.{Paths, StandardCopyOption, Files, Path}
import java.io.File
import java.util.prefs.Preferences
import scala.Some
import nz.ubermouse.processor.SeriesMetaData
import nz.ubermouse.processor.ds.MediaFile
import nz.ubermouse.processor.ds.FileSystemObject

class FileProcessor(parser: MediaParser, metaDataProvider: MetaData, fs: TFileSystem) {
  if(!isAdmin) throw new Error("Class was created with out Administrative permissions. Please re start application as Administrator")
  private case class MetadataTransformation(name: String, files: Iterable[MediaFile], metaDeta: SeriesMetaData)
  private case class FileData(anime: Media, root: FileSystemObject)

  def process(root: File, to: File, titles: Iterable[String]): Iterable[MediaFile] = {
    if(!root.exists()) throw new IllegalArgumentException("'root' must exist")
    if(!to.exists()) throw new IllegalArgumentException("'to' must exist")
    if(!to.isDirectory) throw new IllegalArgumentException("'to' must be a directory")
    if(titles.isEmpty) throw new IllegalArgumentException("'titles' must have at least one item in it")

    val files = {
      if(root.isDirectory) fs.getFilesIn(root)
      else List(FileSystemObject(root, root.getName))

    }
    val foundMedia = parser(fs.getFilesIn(root), titles)

    processMediaFiles(to, foundMedia)

    foundMedia
  }

  private def processMediaFiles(to: File, files: Iterable[MediaFile]) {
    val uniqueSeries = files.groupBy(_.name)
    //todo figure out how to make it work with flatMap or similar
    val combinedWithMetaData = uniqueSeries.map((retrieveMetaData(metaDataProvider) _).tupled).filter(_.isDefined).map(_.get)
    val mediaObjects = combinedWithMetaData.map(sd => {
      sd.files.map(media => FileData(new Media(media, sd.metaDeta,
        metaDataProvider.forEpisode(sd.metaDeta.id, media.season, media.episode).get), media.fso))
    })

    mediaObjects.foreach(files => processGroup(files, to.toPath))
  }

  private def retrieveMetaData(metaDataProvider: MetaData)(name: String, files: Iterable[MediaFile]) = {
    val (correctedName, processor) = Overrides.getPreProcessorForFile(files.head) match {
      case Some(processor) => (processor(files.head).name, Some(processor))
      case None => (name, None)
    }
    val maybeSeriesMetaData = metaDataProvider.search(correctedName)
    if(!maybeSeriesMetaData.isDefined) println("No results from meta data provider, looks like show name needs an override")
    val seriesMetaData = maybeSeriesMetaData.get
    if(maybeSeriesMetaData.isDefined) {
      val correctedFiles = processor.map(p => files.map(f => p(f, seriesMetaData))).getOrElse(files)
      Some(MetadataTransformation(correctedName, correctedFiles, seriesMetaData))
    }
    else
      None
  }

  private def processGroup(group: Iterable[FileData], destinationRoot: Path) {
    case class CombinedMetaData(afd:  FileData, destination: Path)

    def shouldProcess(afd: FileData, destination: Path) = {
      !Files.isSymbolicLink(afd.root.path.toPath) && afd.anime.shouldProcess(destination)
    } 
    
    def needsProcessing(afd: FileData): Boolean = shouldProcess(afd, destinationRoot) match {
      case true => true
      case false =>
        println(s"Skipped copying file: ${afd.root.name}")
        false
    }

    val toBeProcessed = group.filter(needsProcessing)
    val destinationsAttached = toBeProcessed.map(x => CombinedMetaData(x, destinationRoot.resolve(x.anime.getSubpathForEpisode)))
    destinationsAttached.foreach(md => md.afd.anime.process(md.destination))
  }

  //http://stackoverflow.com/a/23538961
  private def isAdmin = {
    val prefs = Preferences.systemRoot()
    try {
      prefs.put("foo", "bar")
      prefs.remove("foo")
      prefs.flush()
      true
    } catch {
      case e: Exception => false
    }
  }
}