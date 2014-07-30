package nz.ubermouse.anime

import java.io.File
import nz.ubermouse.anime.ds.{FileSystemObject, AnimeFile, DirectorySearcher}
import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.Series
import java.nio.file.{StandardCopyOption, Files, Path}

object Main extends App {
  val processFrom = new File(args(0)).toPath
  val processTo = new File(args(1)).toPath

  val animeTitles = List(
    "Seitokai Yakuindomo",
    "Mekakucity Actors",
    "Chuunibyou demo Koi ga Shitai! Ren",
    "Re Hamatora",
    "Sword Art Online II",
    "Space Dandy 2"
  )

  val processor = new AnimeProcessor(processFrom, animeTitles)
  processor.process(processTo)
}
