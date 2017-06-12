package com.inneractive.sentinel.timeseries

import java.io.File

import com.inneractive.sentinel.AnomalyDetector.detectAnomaly
import com.inneractive.sentinel.{DetectorConfig, Direction, OnlyLast}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Created by Richard Grossman on 2017/05/17.
  */
class AnomalyDetectorSpecs extends WordSpecLike with Matchers with MockitoSugar {

  "AnomalyDetector" must {
    "Find anomalies in time series" in {
      val file = new File(this.getClass.getResource("/full.csv").getFile)
      val result = detectAnomaly(
        timeSeries = CsvTimeSeriesProvider.populate(Predicate1(file)).get,
        DetectorConfig(maxAnomaly = 0.02, direction = Direction.both)
      )

      result shouldBe defined
      result.get should have size 131
    }

    "Find anomalies with only last day must return only last XX anomalies in the time series" in {
      val file = new File(this.getClass.getResource("/full.csv").getFile)
      val result = detectAnomaly(
        timeSeries = CsvTimeSeriesProvider.populate(Predicate1(file)).get,
        DetectorConfig( maxAnomaly = 0.02,direction = Direction.both, only_last = OnlyLast.day)
      )

      result shouldBe defined
      result.get should have size 25

      val result2 = detectAnomaly(
        timeSeries = CsvTimeSeriesProvider.populate(Predicate1(file)).get,
        DetectorConfig( maxAnomaly = 0.02,direction = Direction.both, only_last = OnlyLast.day)
      )

      result2 shouldBe defined
      result2.get should have size 4
    }
  }
}
