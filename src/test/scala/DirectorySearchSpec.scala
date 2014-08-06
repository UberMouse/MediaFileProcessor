import java.io.File
import java.nio.file.Path
import nz.ubermouse.processor.ds.{MediaFile, FileSystemObject, FileSystem, MediaParser}
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class DummyFileSystem(dummyFiles:Iterable[FileSystemObject]) extends FileSystem {
  override def getFilesIn(path: File): Iterable[FileSystemObject] = dummyFiles
}

object FSOFactory {
  def apply(name:String) = FileSystemObject(new File(s"C:\\$name"), name)
}

class DirectorySearcherSpec extends UnitSpec {
  "describe DirectorySearcher" - {
    "describe Searching a directory" - {
      "it gives you a list of processor episodes in the supplied directory directory" in {
        val files = List(
          FSOFactory("[Commie] Ping Pong - 09 [347B4335].mkv"),
          FSOFactory("[denpa] Shingeki no Kyojin (Attack on Titan) - 08 [DUB CNHD 480p][8BF1FB1B].mp4"),
          FSOFactory("[HorribleSubs] Gin no Saji S2 - 06 [720p].mkv"),
          FSOFactory("[FTW]_Chuunibyou_demo_Koi_ga_Shitai!_Ren_-_04_[720p][9172389F].mkv"),
          FSOFactory("[ACX]Beck_Mongolian_Chop_Squad_-_03_-_Moon_on_the_Water_[SaintDeath]_[1AEC41E0].mkv"),
          FSOFactory("mostdefinitelynotanime.gif")
        )
        val expectedResults = List(
          ("Ping Pong", 1, 9),
          ("Shingeki no Kyojin", 1, 8),
          ("Gin no Saji", 2, 6),
          ("Chuunibyou demo Koi ga Shitai! Ren", 1, 4),
          ("Beck Mongolian Chop Squad", 1, 3)
        ).zip(files).map{case(anime, fso) => MediaFile(anime._1.toLowerCase, anime._2, anime._3, fso)}
        val titles = List(
          "Ping Pong",
          "Shingeki no Kyojin",
          "Attack on Titan",
          "Gin no Saji",
          "Chuunibyou demo Koi ga Shitai! Ren",
          "BECK Mongolian Chop Squad"
        )
        val parser = new MediaParser()

        val foundFiles = parser(files, titles)

        foundFiles should equal (expectedResults)
      }
    }
  }
}
