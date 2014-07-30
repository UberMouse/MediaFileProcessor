import com.omertron.thetvdbapi.model.Series
import com.omertron.thetvdbapi.TheTVDBApi
import nz.ubermouse.anime.{SeriesMetaData, TvdbMetaData}

class TvdbMetaDataSpec extends UnitSpec {
  class NoArgsTvdb extends TheTVDBApi("")

  def withTvdb(testCode: (TheTVDBApi, TvdbMetaData, String) => Any) {
    val tvdb = mock[NoArgsTvdb]
    val metaData = new TvdbMetaData(tvdb)
    val seriesName = "Gekkan Shoujo Nozaki-kun"

    testCode(tvdb, metaData, seriesName)
  }

  "describe TvdbMetaData" - {
    "describe #search" - {
      "when results are found, gives first result" in withTvdb { (tvdb, metaData, seriesName) =>
        val seriesId = "279804"

        val firstResult = mock[Series]
        (firstResult.getSeriesName _).expects().returns(seriesName)
        (firstResult.getId _).expects().returns(seriesId)

        val results = new java.util.ArrayList[Series]
        val items = List(firstResult, mock[Series])
        items.foreach(results.add)

        (tvdb.searchSeries _).expects(seriesName, "en").returning(results)

        val result = metaData.search(seriesName)
        result.isDefined should be (true)
        result.get should be (SeriesMetaData(seriesName, seriesId))
      }

      "when no results are found, returns None" in withTvdb { (tvdb, metaData, seriesName) =>
        (tvdb.searchSeries _).expects(seriesName, "en").returns(new java.util.ArrayList[Series])

        metaData.search(seriesName) should be (None)
      }
    }
  }
}
