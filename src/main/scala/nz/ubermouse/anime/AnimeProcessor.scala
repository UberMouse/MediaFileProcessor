package nz.ubermouse.anime

import nz.ubermouse.anime.ds.{FileSystemObject, AnimeFile, DirectorySearcher}
import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.Series
import java.nio.file.{Paths, StandardCopyOption, Files, Path}
import java.io.File

class AnimeProcessor(root: Path, animeTitles: List[String]) {

  private case class MetadataTransformation(name: String, files: Iterable[AnimeFile], metaDeta: SeriesMetaData)
  private case class AnimeFileData(anime: Anime, root: FileSystemObject)

  def process(to: Path) {
    val searcher = new DirectorySearcher
    val foundAnime = searcher(root.toFile, animeTitles)
    val tvdb = new TheTVDBApi("CFBFA92BDDCC4F64")
    val metaDataProvider = new TvdbMetaData(tvdb)

    val uniqueSeries = foundAnime.groupBy(_.name)
    val combinedWithMetaData = uniqueSeries.flatMap{case(name, files) => {
      val (correctedName, processor) = Overrides.getPreProcessorForFile(files.head) match {
        case Some(processor) => (processor(files.head).name, Some(processor))
        case None => (name, None)
      }
      val maybeSeriesMetaData = metaDataProvider.search(correctedName)
      val seriesMetaData = maybeSeriesMetaData.get
      if(maybeSeriesMetaData.isDefined) {
        val correctedFiles = processor.map(p => files.map(f => p(f, seriesMetaData))).getOrElse(files)
        Some(MetadataTransformation(correctedName, correctedFiles, seriesMetaData))
      }
      else
        None
    }}
    val animeObjects = combinedWithMetaData.map(sd => {
      sd.files.map(anime => AnimeFileData(new Anime(anime, sd.metaDeta, 
        metaDataProvider.forEpisode(sd.metaDeta.id, anime.season, anime.episode).get), anime.fso))
    })

    animeObjects.foreach(files => processGroup(files, to))
  }

  def processGroup(group: Iterable[AnimeFileData], destinationRoot: Path) {
    var approvedAll = false

    def shouldProcess(afd: AnimeFileData, destination: Path) = {
      !Files.isSymbolicLink(afd.root.path.toPath) && afd.anime.shouldProcess(destination)
    } 
    
    def needsProcessing(afd: AnimeFileData): Boolean = {
      if(shouldProcess(afd, destinationRoot))
        return true

      println(s"Skipped copying file: ${afd.root.name}")
      false
    }
    
    def userApproved(state: (Boolean, List[AnimeFileData]), current: AnimeFileData) = {
      val (approvedAllFiles, approved) = state
      println(s"Processing from ${current.root.path.toPath} to ${destinationRoot.resolve(current.anime.getSubpathForEpisode)}")
      
      if(approvedAllFiles) {
        (true, current :: approved)  
      } else {
        print("[y]es/[n]o/[a]ll of group: ")
        val answer = scala.io.StdIn.readLine()
        answer match {
          case "y" => (false, current :: approved)
          case "n" => (false, approved)
          case "a" => {
            (true, current :: approved)
          }
          case _ =>  {
            println(s"No match found, answer: $answer")
            (false, approved)
          }
        }
      }
    }

    val toBeProcessed = group.filter(needsProcessing)
    val approvedFiles = toBeProcessed.foldLeft(
      (false, List[AnimeFileData]())
    )(userApproved)._2
    val destinationsAttached = approvedFiles.map(x => (x, destinationRoot.resolve(x.anime.getSubpathForEpisode)))
    destinationsAttached.foreach{case(afd: AnimeFileData, destination: Path) => afd.anime.process(destination)}
  }
}
