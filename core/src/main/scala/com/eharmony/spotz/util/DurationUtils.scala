package com.eharmony.spotz.util

import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder

/**
  * @author vsuthichai
  */
object DurationUtils {
  val formatter = new PeriodFormatterBuilder()
    .appendDays().appendSuffix("d")
    .appendHours().appendSuffix("h")
    .appendMinutes().appendSuffix("m")
    .appendSeconds().appendSuffix("s")
    .appendMillis().appendSuffix("ms")
    .toFormatter

  /**
    * Utility function to format a duration.
    *
    * @param duration
    * @return
    */
  def format(duration: Duration) = formatter.print(duration.toPeriod)
}
