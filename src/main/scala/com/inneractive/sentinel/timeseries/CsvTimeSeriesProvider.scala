package com.inneractive.sentinel.timeseries

import java.io.File

import com.github.nscala_time.time.Imports._
import com.inneractive.sentinel.csvDateFormat
import com.inneractive.sentinel.timeseries.myType.TS

import scala.io.Source


/**
  * Used as to pass parameters to the CsvTimeSeriesProvider
  *
  * @param file the csv file
  * @param separator the csv file separator
  */
case class Predicate1(file : File, separator : String = ",") extends Predicate

object CsvTimeSeriesProvider extends TimeSeriesProvider[Predicate1] {

  /**
    * Read a CSV file formatted like
    * Timestamp<separator>Value
    *
    * The expected date format is YYYY-MM-dd HH:mm:ss
    * @param predicate a predicte1
    * @return a Vector of the TimeSeries sorted by date
    */
  override def populate(predicate : Predicate1): Option[Vector[TS]] = {
    val seqData = Source.fromFile(predicate.file).getLines() map { r =>
      val token = r.split(predicate.separator)
      (csvDateFormat.parseDateTime(token(0)), token(1).toDouble)
    }

    Some(seqData.toVector.sortWith(_._1 < _._1))
  }

}
