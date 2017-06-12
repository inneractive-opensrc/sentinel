package com.inneractive.sentinel.logger

import com.inneractive.sentinel.timeseries.myType.TS

/**
  * Very simple logger trait
  */
trait AnomalyLogger {
  def log(anomalies : Vector[TS])
}
