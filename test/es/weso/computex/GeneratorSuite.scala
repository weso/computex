package es.weso.computex

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import es.weso.computex.profile.Profile
import es.weso.computex.profile.Passed
import es.weso.computex.profile.NotPassed

class GeneratorSuite extends FunSpec 
	with ShouldMatchers 
	with SparqlSuite {

val conf : Config = ConfigFactory.load()

  describe("Generator") {
   describe("An example with one indicator, one country and one year should pass Cube integrity checks") {
	   val g = Generator(1,1,1)
	   val profile = Profile.Cube
	   val (vr,m) = profile.validate(g.model,true,true)
	   vr match {
	     case Passed(_) => info("Validates")
	     case NotPassed(ps) => fail("Does not validate: " + ps)
	   }
   }

   describe("An example with two indicators, two countries and one year should pass Computex integrity checks") {
	   val g = Generator(2,2,1)
	   val profile = Profile.Computex
	   val (vr,m) = profile.validate(g.model,true,true)
	   vr match {
	     case Passed(_) => info("Validates")
	     case NotPassed(ps) => fail("Does not validate: " + ps)
	   }
   }

   describe("An example with two indicators, three countries and 4 years should pass Cube integrity checks") {
	   val g = Generator(2,3,4)
	   val profile = Profile.Computex
	   val (vr,m) = profile.validate(g.model,true,true)
	   vr match {
	     case Passed(_) => info("Validates")
	     case NotPassed(ps) => fail("Does not validate: " + ps)
	   }
   }

   ignore("An example with twenty indicators, 30 countries and 4 years should pass Cube integrity checks") {
	   val g = Generator(20,30,4)
	   val profile = Profile.Computex
	   val (vr,m) = profile.validate(g.model,true,true)
	   vr match {
	     case Passed(_) => info("Validates")
	     case NotPassed(ps) => fail("Does not validate: " + ps)
	   }
   }
}

}