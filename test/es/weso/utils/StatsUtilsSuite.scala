package es.weso.utils

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import es.weso.utils.StatsUtils._


/*
 * The following tests could be declared in another Suite without the need for 
 * Computex Class
 */
class  StatsUtilsSuite
	extends FunSpec 
	with ShouldMatchers {
 
 describe("CalculateMeanSD") {
   it("Should calculate mean and sd of a list of values") {
     val ls : Seq[Double] = Seq(2,3,4)
     val (mean,sd) = calculateMeanSD(ls)
     mean should equal(3.0)
     sd should equal(1.0)
   }
 }
 
}

