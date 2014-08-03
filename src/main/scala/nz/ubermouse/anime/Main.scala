package nz.ubermouse.anime

import java.io.File
import nz.ubermouse.anime.ds.{FileSystemObject, AnimeFile, DirectorySearcher}
import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.Series
import java.nio.file.{StandardCopyOption, Files, Path}
import scaldi.Injectable

object Main extends App with Injectable {
  val processFrom = new File(args(0))
  val processTo = new File(args(1))

  if(!processFrom.exists) throw new IllegalArgumentException("processFrom (First argument) must be an existing directory")
  if(!processFrom.isDirectory) throw new IllegalArgumentException("processFrom (First argument) must be a directory")
  if(!processTo.isDirectory) throw new IllegalArgumentException("processTo (Second argument) must be a directory")

  implicit val appModule = new ProcessorModule

  val animeTitles = List(
    "Seitokai Yakuindomo",
    "Mekakucity Actors",
    "Chuunibyou demo Koi ga Shitai! Ren",
    "Re Hamatora",
    "Sword Art Online II",
    "Space Dandy 2",
    "Tokyo Ghoul",
    "Glasslip",
    "Aldnoah.Zero",
    "Akame ga Kill",
    "Gekkan Shoujo Nozaki-Kun"
  )

  val processor = inject [AnimeProcessor]
  processor.process(processFrom, processTo, animeTitles)
}
