package com.inneractive.sentinel.timeseries

import java.io.File

import com.github.nscala_time.time.Imports._
import com.inneractive.sentinel
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

import scala.io.Source

/**
  * Created by Richard Grossman on 2017/05/15.
  */
class TimeSeriesSpecs extends WordSpecLike with Matchers with MockitoSugar {
  def getFile(fileName : String) = new File(this.getClass.getResource(fileName).getFile)

  "CsvTimeSeries" must {
    "Read a csv file with fixed format and return a TimeSeries set of point" in {
      val ts = CsvTimeSeriesProvider.populate(Predicate1(getFile("/data.csv")))
      ts.get should contain inOrder(
        (DateTime.parse("1980-10-05 13:32:00", sentinel.csvDateFormat), 148.456),
        (DateTime.parse("1980-10-05 13:33:00", sentinel.csvDateFormat), 150.201),
        (DateTime.parse("1980-10-05 13:34:00", sentinel.csvDateFormat), 147.082)
      )
    }

    "Reorder by ascending time" in {
      val ts = CsvTimeSeriesProvider.populate(Predicate1(getFile("/data-no-order.csv")))
      ts.get should contain inOrder(
        (DateTime.parse("1980-10-05 13:32:00", sentinel.csvDateFormat), 148.456),
        (DateTime.parse("1980-10-05 13:33:00", sentinel.csvDateFormat), 150.201),
        (DateTime.parse("1980-10-05 13:34:00", sentinel.csvDateFormat), 147.082)
      )
    }
  }
}
