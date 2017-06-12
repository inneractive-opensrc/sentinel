package com.inneractive.sentinel.timeseries

import com.github.nscala_time.time.Imports._
import com.inneractive.sentinel.timeseries.myType.TimeSeries


package object myType {
  type TS = (DateTime, Double)
  type TimeSeries = Vector[TS]
}


trait Predicate


trait TimeSeriesProvider[P <: Predicate] {
  def populate(predicate : P): Option[TimeSeries]
}




