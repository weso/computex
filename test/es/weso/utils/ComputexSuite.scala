package es.weso.utils

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import es.weso.utils.ComputeTools._

/*
 * The following tests could be declared in another Suite without the need for 
 * Computex Class
 */
class  ComputeToolsSuite
	extends FunSpec 
	with ShouldMatchers {
 
 describe("ComputeDebug") {
   it("Should execute a single list of steps") {
     val s1 = (x:Int) => x + 3
     def s2 = (x: Int) => x * 4
     def merge = (x:Int,y:Int) => x + y
     val ls = Seq(s1,s2)
     computeDebug(ls,3,merge) should be((45,List(6,36)))
   }
 }
 
  describe("Compute") {
   it("Should execute a single list of steps") {
     val s1 = (x:Int) => x + 3
     def s2 = (x: Int) => x * 4
     def merge = (x:Int,y:Int) => x + y
     val ls = Seq(s1,s2)
     compute(ls,3,merge) should be(45)
   }
 } 

}

