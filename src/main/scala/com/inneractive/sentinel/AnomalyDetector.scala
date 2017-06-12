package com.inneractive.sentinel

import java.util.concurrent.TimeUnit

import com.github.nscala_time.time.Imports._
import com.github.servicenow.ds.stats.stl.SeasonalTrendLoess
import com.inneractive.sentinel.timeseries.myType.{TS, TimeSeries}
import org.apache.commons.math3.distribution.TDistribution
import org.joda.time.Seconds
import org.slf4j.LoggerFactory
import scilube.Matlib

object Direction extends Enumeration {
  val positive, negative, both = Value
}

object OnlyLast extends Enumeration {
  val all, day, hr = Value
}

object Threshold extends Enumeration {
  val none, med_max, p95, p99 = Value
}

/**
  * Anomaly detector algorithm
  * this code is a rewrite of the original R code @see
  * <a href="https://github.com/twitter/AnomalyDetection" target="_top">AnomalyDetection<a>
  *
  * @param maxAnomaly Fix a percentage of allowed max anomalies 0<X<1
  * @param direction filter the anomaly that are is wrong direction value are Direction
  * @param alpha The level of statistical significance with which to accept or reject anomalies
  * @param only_last Find and report anomalies only within the last day or hr in the time series
  * @param threshold Only report positive going anoms above the threshold specified. Options are: {'None' | 'med_max' | 'p95' | 'p99'}
  * @param piecewise_median_period_weeks he piecewise median time window as described in Vallis, Hochenbaum, and Kejariwal (2014). Defaults to 2.
  * @param verbose log the steps in the evaluation
  */
case class DetectorConfig(maxAnomaly: Double = 0.1,
                          direction: Direction.Value = Direction.positive,
                          alpha: Double = 0.05,
                          only_last: OnlyLast.Value = OnlyLast.all,
                          threshold: Threshold.Value = Threshold.none,
                          piecewise_median_period_weeks: Int = 2,
                          verbose: Boolean = false)

object AnomalyDetector {
  val log = LoggerFactory.getLogger(this.getClass)

  def detectAnomaly(timeSeries: TimeSeries, config : DetectorConfig) : Option[TimeSeries] = {


    var max_anom = config.maxAnomaly

    if (max_anom == 0.0) log.warn("0 max_anoms results in max_outliers being 0.")
    else if (max_anom < 0.0) throw new IllegalArgumentException("max_anoms must be positive.")
    else if (max_anom > 0.49) throw new IllegalArgumentException("maxAnomaly must be less " +
      s"than 50% of the data points (max_anoms = ${Math.round(max_anom * timeSeries.size)}, " +
      s"data_points=${timeSeries.size}).")

    if (!(0.01 <= config.alpha || config.alpha <= 0.1)) {
      if (config.verbose) log.info("Warning: alpha is the statistical signifigance, and is usually between 0.01 and 0.1")
    }

    if (config.piecewise_median_period_weeks < 2)
      throw new IllegalAccessException("piecewise_median_period_weeks must be at greater than 2 weeks")

    val granularity = getGranularity(timeSeries)

    val ts = if (granularity == TimeUnit.SECONDS) {
      timeSeries.map(d => (d._1.withMillisOfSecond(0).withSecondOfMinute(0), d._2))
        .groupBy(v => v._1)
        .map(v => (v._1, v._2.foldLeft(0.0)((v1, v2) => v1 + v2._2)))
        .toVector
    } else timeSeries

    val period = granularity match {
      case TimeUnit.MINUTES => 1440
      case TimeUnit.HOURS => 24
      case TimeUnit.DAYS => 7
      case _ => throw new IllegalArgumentException(s"Period : $granularity is not supported")
    }

    max_anom = if (max_anom < 1.0 / ts.size.toDouble) 1.0 / ts.size.toDouble else max_anom

    val anomalyDirection = config.direction match {
      case Direction.positive => (true, true)
      case Direction.negative => (true, false)
      case Direction.both => (false, true)
    }

    val s_h_esd_timestamps = detectAnomalies(ts, numObsPerPeriod = period, k = max_anom,
      alpha = config.alpha, oneTail = anomalyDirection._1, upperTail = anomalyDirection._2,
      verbose = config.verbose)

    val result = s_h_esd_timestamps.getOrElse(throw new Exception("Failure to detect anomalies"))
    val anomalies = ts.filter(t => result._1.unzip._1.contains(t._1))

    val maxDaily = timeSeries.map(d => (d._1.toLocalDate, d._2))
      .groupBy(v => v._1)
      .map(v => v._2.maxBy(x => x._2))
      .unzip._2.toArray

    val thresh = config.threshold match {
      case Threshold.med_max => Matlib.median(maxDaily)
      case Threshold.p95 => Matlib.quantile(maxDaily, 0.95)
      case Threshold.p99 => Matlib.quantile(maxDaily, 0.99)
      case _ => 0
    }

    val anoms = anomalies.filter(a => config.threshold == Threshold.none ||  a._2 >= thresh)

    val results = config.only_last match {
      case OnlyLast.day =>
        val maxDay = anoms.map(v => v._1.toLocalDate).last
        anoms.filter(v => v._1.toLocalDate == maxDay)
      case OnlyLast.hr =>
        val maxHour = anoms.map(v => v._1.withSecondOfMinute(0).withMinuteOfHour(0)).last
        anoms.filter(v => v._1.withSecondOfMinute(0).withMinuteOfHour(0) == maxHour)
      case _ => anoms
    }

    Some(results)
  }


  def detectAnomalies(data: TimeSeries, numObsPerPeriod: Int,
                      k: Double = 0.48, alpha: Double = 0.05, useDecomp: Boolean = true,
                      useESD: Boolean = false, oneTail: Boolean = true, upperTail: Boolean = true,
                      verbose: Boolean = false): Option[(Array[TS], Array[TS])] = {

    val n = data.length
    var d = data.toArray

    if (d.size < numObsPerPeriod * 2) throw new IllegalArgumentException("Anomaly detection needs at least 2 periods of data")

    val times = d.unzip._1
    val values = d.unzip._2

    val seasonalWidth = 10 * n +1
    val seasonalJump = Math.ceil(seasonalWidth / 10)
    val trendWidth = Math.ceil(1.5 * numObsPerPeriod / (1 - 1.5/seasonalWidth) )
    val trendJump = Math.ceil(trendWidth / 10)

    val builder = new SeasonalTrendLoess.Builder
    val smoother = builder.setPeriodLength(numObsPerPeriod)
      .setSeasonalWidth(seasonalWidth)
      .setSeasonalJump(seasonalJump.toInt)
      .setSeasonalDegree(0)
      .setTrendDegree(1)
      .setTrendWidth(trendWidth.toInt)
      .setTrendJump(trendJump.toInt)
      .setRobust()
      .buildSmoother(values)

    val stl = smoother.decompose()

    val seriesMedian = Matlib.median(values)
    d = times.zip(values.zip(stl.getSeasonal).map(v => v._1 - v._2 - seriesMedian))

    val expectedValues = times.zip(stl.getTrend.zip(stl.getSeasonal).map(v => trunc(v._1 + v._2)))

    val maxOutliers = trunc(d.length.toDouble * k)
    if (maxOutliers == 0)
      throw new Exception(s"With longterm=TRUE, AnomalyDetection splits the data into 2 week periods by default. You have ${data.size} " +
        s"observations in a period, which is too few. Set a higher piecewise_median_period_weeks.")

    val R_idx: Array[TS] = Array.fill[TS](maxOutliers.toInt)(DateTime.now(),0)

    // Create based on maxOutliers from data
    d.take(maxOutliers.toInt).copyToArray(R_idx)

    var num_anom = 0
    for (i <- 1 to maxOutliers.toInt) yield {
      if (verbose) log.info(s"$i/$maxOutliers completed")

      val median = Matlib.median(d.unzip._2)
      var ares = if (oneTail) {
        if (upperTail) d.map(v => v._2 - median)
        else d.map(v => median - v._2)
      } else d.map(v => Math.abs(v._2 - median))

      val dataSigma = mad(d.unzip._2)
      if (dataSigma != 0) {
        ares = ares.map(v => v / dataSigma)
        val maxInAres = ares.max
        val temp_max_id = ares.indexWhere(v => v == maxInAres)

        R_idx(i - 1) = (d(temp_max_id)._1,d(temp_max_id)._2)
        d = d.filter(v => v != R_idx(i - 1))

        val p = if (oneTail) 1 - alpha / (n - i + 1)
        else 1 - alpha / (2 * (n - i + 1))

        val t = qt(p, n - i - 1)
        val lam = t * (n - i) / Math.sqrt((n - i - 1 + Math.pow(t, 2)) * (n - i + 1))

        if (maxInAres > lam) num_anom = i
      }
    }

    if (num_anom > 0) {
      Some((R_idx.take(num_anom), expectedValues))
    } else None
  }


  def mad(values: Array[Double]): Double = {
    val MADSCALEFACTOR = 1.4826

    val median = Matlib.median(values)
    val data = values.map(v => Math.abs(v - median))
    val median2 = Matlib.median(data)
    MADSCALEFACTOR * median2
  }

  def qt(p: Double, df: Double) = new TDistribution(df).inverseCumulativeProbability(p)

  def trunc(d: Double): Double = {
    BigDecimal(d).setScale(0, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def getGranularity(ts: TimeSeries): TimeUnit = {
    val last2Point = ts.takeRight(2).toArray

    val granularity = Seconds.secondsBetween(last2Point(0)._1, last2Point(1)._1)

    granularity.getSeconds match {
      case g if g >= 86400 => TimeUnit.DAYS
      case g if g >= 3600 => TimeUnit.HOURS
      case g if g >= 60 => TimeUnit.MINUTES
      case g if g >= 1 => TimeUnit.SECONDS
      case _ => TimeUnit.MILLISECONDS
    }
  }

}
