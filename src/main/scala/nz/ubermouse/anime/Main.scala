package nz.ubermouse.anime

import java.io.File
import nz.ubermouse.anime.ds.{FileSystemObject, AnimeFile, DirectorySearcher}
import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.Series
import java.nio.file.{StandardCopyOption, Files, Path}

object Main extends App {
  val processFrom = new File(args(0))
  val processTo = new File(args(1))

  val animeTitles = List(
    "Seitokai Yakuindomo",
    "Mekakucity Actors",
    "Chuunibyou demo Koi ga Shitai! Ren",
    "Re Hamatora",
    "Sword Art Online II",
    "Space Dandy 2",
    "Tokyo Ghoul",
    "Glasslip"
  )

  val tvdb = new TheTVDBApi("CFBFA92BDDCC4F64")
  val metaData = new TvdbMetaData(tvdb)
  val processor = new AnimeProcessor(processFrom, animeTitles)(new DirectorySearcher, metaData)
  processor.process(processTo)
}
