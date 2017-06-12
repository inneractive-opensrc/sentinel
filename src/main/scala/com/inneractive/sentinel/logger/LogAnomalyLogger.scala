package com.inneractive.sentinel.logger

import com.inneractive.sentinel.timeseries.myType.TS
import org.slf4j.{Logger, LoggerFactory}

/**
  * Write the anomalies into a log file
  */
class LogAnomalyLogger extends AnomalyLogger {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def log(anomalies: Vector[TS]): Unit = anomalies foreach {a =>
    logger.info(s"Anomaly detected at ${a._1} value : ${a._2}")
  }
}
