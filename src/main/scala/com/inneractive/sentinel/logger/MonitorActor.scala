package com.inneractive.sentinel.logger

import akka.actor.Actor
import com.inneractive.sentinel.timeseries.myType.TimeSeries
import com.typesafe.config.Config

/**
  * Actor sending the detection results to the logger
  */
class MonitorActor extends Actor {
  val config: Config = context.system.settings.config
  val loggerProvider: AnomalyLogger = Class.forName(config.getString("sentinel.detector.logger.provider")).newInstance()
    .asInstanceOf[AnomalyLogger]

  override def receive: Receive = {
    case ts : TimeSeries => loggerProvider.log(ts)
  }
}
