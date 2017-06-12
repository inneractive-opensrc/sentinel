package com.inneractive

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

/**
  * Created by Richard Grossman on 2017/05/10.
  */
package object sentinel {
  val csvDateFormat: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC()
  val fileIdDateFormat: DateTimeFormatter = DateTimeFormat.forPattern("YYYYMMddHHmmss").withZoneUTC()
}
