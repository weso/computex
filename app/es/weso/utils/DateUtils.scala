package es.weso.utils

import java.util.Calendar
import java.text.SimpleDateFormat

object DateUtils {
  
  def getCurrentTimeAsString() : String = {
    val actual = Calendar.getInstance().getTime()
    
    val yearFormat = new SimpleDateFormat("yyyy")
    val monthFormat = new SimpleDateFormat("MM")
    val dayFormat = new SimpleDateFormat("dd")
    val currentYear = yearFormat.format(actual)
    val currentMonth = monthFormat.format(actual)
    val currentDay = dayFormat.format(actual)
    currentYear + "-" + currentMonth + "-" + currentDay
  }

}