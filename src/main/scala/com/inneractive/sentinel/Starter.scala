package com.inneractive.sentinel

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import com.inneractive.sentinel.logger.MonitorActor
import org.joda.time.format.ISOPeriodFormat
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration

object Starter extends App {
  val logger = LoggerFactory.getLogger(this.getClass)

  val actorSystem = ActorSystem("Sentinel")
  implicit val executionContext = actorSystem.dispatcher

  val monitorActor = actorSystem.actorOf(Props(classOf[MonitorActor]), "MonitorActor")
  val anomalyActor = actorSystem.actorOf(Props(classOf[AnomalyDetectorActor], monitorActor), "AnomalyDetectorActor")

  val configPeriod = actorSystem.settings.config.getString("sentinel.scheduler.interval")
  val period = ISOPeriodFormat.standard().parsePeriod(configPeriod)
  val interval = new FiniteDuration(period.toStandardDuration.getMillis, TimeUnit.MILLISECONDS)

  logger.info("Start Scheduler now ")

  import scala.concurrent.duration._
  val detectorConfig = DetectorConfig(maxAnomaly = 0.02, direction = Direction.both, only_last = OnlyLast.hr)
  val cancellablePostTask = Some(actorSystem.scheduler.schedule(10.seconds, interval, anomalyActor, TaskNow(detectorConfig)))

  sys.addShutdownHook {
    logger.info("Shutdown the tasker")
    val status = cancellablePostTask map (_.cancel())
    logger.info(s"Stop the scheduler status : ${status.getOrElse("true")}")
    actorSystem.terminate()
  }
}
