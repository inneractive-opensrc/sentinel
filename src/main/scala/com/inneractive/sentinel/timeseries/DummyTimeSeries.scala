package com.inneractive.sentinel.timeseries

import com.github.nscala_time.time.Imports._
import com.inneractive.sentinel.timeseries.myType.TimeSeries

/**
  * Dummy TimeSeries populate a fixed time series with some anomalies
  *
  * Created by Richard Grossman on 2017/05/10.
  */

case class EmptyPredicate() extends Predicate

object DummyTimeSeries extends TimeSeriesProvider[EmptyPredicate] {
  override def populate(predicate : EmptyPredicate): Option[TimeSeries] = {
    val now = DateTime.now()

    val v1 = (for (i <- 0 until 200) yield now.plusHours(i) -> 10.0) :+ now.plusHours(200) -> 1000.0 :+ now.plusHours(201) -> -1000.0
    val v2 = (for (i <- 202 until 500) yield now.plusHours(i) -> 10.0) :+ now.plusHours(500) -> 1000.0 :+ now.plusHours(501) -> -1000.0

    val v3 = v1 ++ v2

    Some(v3.toVector)
  }
}
