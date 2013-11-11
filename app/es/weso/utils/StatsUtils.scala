package es.weso.utils

import Math._

object StatsUtils {
  
  def calculateMeanSD(values : Seq[Double], highLow: Boolean = true) : (Double, Double) = {
    val count : Double = values.length
    val sum : Double = values.sum
    val mean : Double = sum / count
    val sumSquares = values.foldLeft(0.0)((r,x) => r + (x - mean)*(x - mean))
    val sd = sqrt(sumSquares/(count - 1.0)) 
    (mean, sd)
  }

}