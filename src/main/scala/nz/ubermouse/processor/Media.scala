package nz.ubermouse.processor

import nz.ubermouse.processor.ds.{FileSystemObject, MediaFile}
import com.omertron.thetvdbapi.model.Series
import java.nio.file.{StandardCopyOption, Files, Path, Paths}

class Media(animeFile: MediaFile, series: SeriesMetaData, episode: EpisodeMetaData) {
  def getSubpathForEpisode = {
    Paths.get("")
      .resolve(cleanString(series.name))
      .resolve(s"Season ${animeFile.season}")
      .resolve(getAnimeFileName)
  }

  def getAnimeFileName = {
    f"${cleanString(series.name)} - S${animeFile.season}%02dE${animeFile.episode}%02d - ${cleanString(episode.name)}.$extractFileExtension"
  }

  def process(destinationRoot: Path) = {
    if(shouldProcess(destinationRoot)) {
      destinationRoot.getParent.toFile.mkdirs()
      Files.move(animeFile.fso.path.toPath, destinationRoot, StandardCopyOption.ATOMIC_MOVE)
      Files.createSymbolicLink(animeFile.fso.path.toPath, destinationRoot)
      println(s"Created link from ${animeFile.fso.path.toPath}\n to $destinationRoot")
    }
  }

  def shouldProcess(destinationRoot: Path) = {
    val destinationFile = destinationRoot.resolve(getSubpathForEpisode)
    var process = true
    if(destinationFile.toFile.exists)
      process = animeFile.fso.path.length > destinationFile.toFile.length

    process
  }

  def extractFileExtension: String = {
    animeFile.fso.name.split("\\.").last
  }

  def cleanString(name: String) = {
    val regex = """[\/:*?"|]""".r
    regex.replaceAllIn(name.replaceAll("_", " "), "")
  }
}
