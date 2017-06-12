package com.inneractive.sentinel.timeseries

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.inneractive.sentinel._
import com.inneractive.sentinel.timeseries.myType.TimeSeries
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.mockito.MockitoSugar

/**
  * Created by Richard Grossman on 2017/06/01.
  */
class AnomalyDetectorActorSpecs extends TestKit(ActorSystem("MySpecs")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfter with MockitoSugar {

  val monitorActorProbe = TestProbe()

  val actor = TestActorRef(Props(classOf[AnomalyDetectorActor], monitorActorProbe.ref))
  val detectorConfig = DetectorConfig(maxAnomaly = 0.02, direction = Direction.both, only_last = OnlyLast.hr)

  "Anomaly Detector" must {
    "actor get a task message" in {
      actor ! TaskNow(detectorConfig)

      monitorActorProbe.expectMsgClass(classOf[TimeSeries])
    }

    "actor can get a time series from message" in {
      val ts = DummyTimeSeries.populate(EmptyPredicate())
      ts foreach (ts => actor ! DetectAnomalies(ts, detectorConfig))
    }
  }

}
