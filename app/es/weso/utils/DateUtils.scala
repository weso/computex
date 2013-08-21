package es.weso.utils

import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtils {

  val yearFormat = new SimpleDateFormat("yyyy")
  val monthFormat = new SimpleDateFormat("MM")
  val dayFormat = new SimpleDateFormat("dd")

  def getCurrentTimeAsString(): String = {
    val actual = Calendar.getInstance().getTime()

    val currentYear = yearFormat.format(actual)
    val currentMonth = monthFormat.format(actual)
    val currentDay = dayFormat.format(actual)
    currentYear + "-" + currentMonth + "-" + currentDay
  }

}