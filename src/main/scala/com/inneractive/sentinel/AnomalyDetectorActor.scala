package com.inneractive.sentinel

import java.io.File

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import akka.routing.FromConfig
import com.inneractive.sentinel.logger.AnomalyLogger
import com.inneractive.sentinel.timeseries.myType.TimeSeries
import com.inneractive.sentinel.timeseries.{CsvTimeSeriesProvider, Predicate1}
import com.typesafe.config.Config

/**
  * Receive a TimeSeries message object and run the anomaly detection
  *
  * Anomaly detection using twitter anomaly detector
  * @see https://github.com/twitter/AnomalyDetection
  *
  */
class AnomalyDetectorActor(monitorActor : ActorRef) extends Actor with akka.actor.ActorLogging {

  override def receive: Receive = {
    case TaskNow(detectorConfig) =>
      log.info("Start anomaly detection")

      val file = new File(this.getClass.getResource("/full.csv").getFile)
      val timeSeries = CsvTimeSeriesProvider.populate(Predicate1(file))

      timeSeries foreach (ts => detectAnomalies(ts, detectorConfig))

    case DetectAnomalies(ts, detectorConfig) => detectAnomalies(ts, detectorConfig)
  }

  def detectAnomalies(ts : TimeSeries, config : DetectorConfig) = {
    val anomalies = AnomalyDetector.detectAnomaly(ts, config)

    anomalies foreach (ts => monitorActor ! ts)
  }
}

case class TaskNow(config : DetectorConfig)
case class DetectAnomalies(ts : TimeSeries, config : DetectorConfig)

trait ActorBuilder {
  def create(context : ActorContext) : Seq[ActorRef]
}

object AnomalyDetectorBuilder extends ActorBuilder {
  override def create(context: ActorContext): Seq[ActorRef] =
    Seq(context.actorOf(FromConfig.props(Props(classOf[AnomalyDetectorActor])), "AnomalyDetector"))
}
