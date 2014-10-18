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

    val foundMedia = parser(files, titles)

    processMediaFiles(to, foundMedia)

    foundMedia
  }

  private def processMediaFiles(to: File, files: Iterable[MediaFile]) {
    def createMediaObject(sd: MetadataTransformation) = sd.files.map{media =>
      val episode = metaDataProvider.forEpisode(sd.metaDeta.id, media.season, media.episode)
      FileData(new Media(media, sd.metaDeta, episode.getOrElse(EpisodeMetaData("error"))), media.fso)
    }

    val uniqueSeries = files.groupBy(_.name)
    val metaDataTransformer = retrieveMetaData(metaDataProvider) _
    val combinedWithMetaData = uniqueSeries.flatMap{case(title, files) => metaDataTransformer(title, files)}
    val mediaObjects = combinedWithMetaData.par.map(createMediaObject)

    mediaObjects.foreach(files => processGroup(files, to.toPath))
  }

  private def retrieveMetaData(metaDataProvider: MetaData)(name: String, files: Iterable[MediaFile]) = {
    val head = files.head
    if(!files.forall(_.name == head.name)) throw new IllegalArgumentException("All files must be for the same series")

    val (correctedName, processor) = Overrides.getPreProcessorForFile(head) match {
      case Some(processor) => (processor(files.head).name, Some(processor))
      case None => (name, None)
    }

    metaDataProvider.search(correctedName).map { seriesMetaData =>
      val correctedFiles = processor.map(p => files.map(f => p(f, seriesMetaData))).getOrElse(files)
      Some(MetadataTransformation(correctedName, correctedFiles, seriesMetaData))
    }.getOrElse(None)
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
