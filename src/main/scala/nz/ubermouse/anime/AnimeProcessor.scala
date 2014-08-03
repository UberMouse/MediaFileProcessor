package nz.ubermouse.anime

import nz.ubermouse.anime.ds.{FileSystemObject, AnimeFile, DirectorySearcher}
import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.Series
import java.nio.file.{Paths, StandardCopyOption, Files, Path}
import java.io.File

class AnimeProcessor(searcher: DirectorySearcher, metaDataProvider: MetaData) {

  private case class MetadataTransformation(name: String, files: Iterable[AnimeFile], metaDeta: SeriesMetaData)
  private case class AnimeFileData(anime: Anime, root: FileSystemObject)

  def process(root: File, to: File, animeTitles: List[String]) {
    if(!root.exists()) throw new IllegalArgumentException("'root' must exist")
    if(!root.isDirectory) throw new IllegalArgumentException("'root' must be a directory")
    if(!to.exists()) throw new IllegalArgumentException("'to' must exist")
    if(!to.isDirectory) throw new IllegalArgumentException("'to' must be a directory")
    if(animeTitles.isEmpty) throw new IllegalArgumentException("'animeTitles' must have at least one item in it")

    val foundAnime = searcher(root, animeTitles)
    val uniqueSeries = foundAnime.groupBy(_.name)
    //todo figure out how to make it work with flatMap or similar
    val combinedWithMetaData = uniqueSeries.map((retrieveMetaData(metaDataProvider) _).tupled).filter(_.isDefined).map(_.get)
    val animeObjects = combinedWithMetaData.map(sd => {
      sd.files.map(anime => AnimeFileData(new Anime(anime, sd.metaDeta, 
        metaDataProvider.forEpisode(sd.metaDeta.id, anime.season, anime.episode).get), anime.fso))
    })

    animeObjects.foreach(files => processGroup(files, to.toPath))
  }

  private def retrieveMetaData(metaDataProvider: MetaData)(name: String, files: Iterable[AnimeFile]) = {
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

  private def processGroup(group: Iterable[AnimeFileData], destinationRoot: Path) {
    case class CombinedMetaData(afd:  AnimeFileData, destination: Path)

    def shouldProcess(afd: AnimeFileData, destination: Path) = {
      !Files.isSymbolicLink(afd.root.path.toPath) && afd.anime.shouldProcess(destination)
    } 
    
    def needsProcessing(afd: AnimeFileData): Boolean = shouldProcess(afd, destinationRoot) match {
      case true => true
      case false =>
        println(s"Skipped copying file: ${afd.root.name}")
        false
    }

    val toBeProcessed = group.filter(needsProcessing)
    val destinationsAttached = toBeProcessed.map(x => CombinedMetaData(x, destinationRoot.resolve(x.anime.getSubpathForEpisode)))
    destinationsAttached.foreach(md => md.afd.anime.process(md.destination))
  }
}
