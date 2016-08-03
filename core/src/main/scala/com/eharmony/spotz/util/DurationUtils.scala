package com.eharmony.spotz.util

import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder

/**
  * Created by vsuthichai on 8/2/16.
  */
object DurationUtils {
  val formatter = new PeriodFormatterBuilder()
    .appendDays().appendSuffix("d")
    .appendHours().appendSuffix("h")
    .appendMinutes().appendSuffix("m")
    .appendSeconds().appendSuffix("s")
    .appendMillis().appendSuffix("ms")
    .toFormatter

  def format(duration: Duration) = formatter.print(duration.toPeriod)
}
