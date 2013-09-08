package es.weso.computex.profile

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.util.FileManager
import scala.collection.JavaConversions._
import com.hp.hpl.jena.vocabulary.RDF
import java.io.FileOutputStream
import java.io.File
import java.io.StringWriter
import es.weso.utils.JenaUtils._
import java.net.URI
import scala.io.Source
import es.weso.utils._
import es.weso.computex.Passed
import es.weso.computex.NotPassed

class ProfileParserSuite 
	extends FunSpec 
	with ShouldMatchers {

  val conf : Config = ConfigFactory.load()
  
  val demoCubeURI			= ConfigUtils.getName(conf,"demoCubeURI")
  val demoComputexURI		= ConfigUtils.getName(conf,"demoURI")
  val demoComputexAbbrURI	= ConfigUtils.getName(conf,"demoAbbrURI")
  
  val cubeProfile 		= ConfigUtils.getName(conf, "cubeProfile")
  val computexProfile 	= ConfigUtils.getName(conf, "computexProfile")

  describe("Profile Parser") {
   it("Should obtain a list of validators for RDF Data Cube") {
     val profile = Profile.Cube
     profile.validators.length should be(23)
   }

   it("Should obtain a list of validators for Computex") {
     val profile = Profile.Computex
     profile.validators.length should be(1)
   }

   it("Should obtain a list of all validators for Computex") {
     val profile = Profile.Computex
     profile.allValidators.length should be(24)
   }

  it("Should obtain a list of expanders for RDF Data Cube profile") {
     val profile = Profile.Cube
     profile.expanders.length should be(2)
   }

  it("Should obtain a list of expanders for Computex profile") {
     val profile = Profile.Computex
     profile.expanders.length should be(1)
   }

  it("Should validate demo of RDF Data Cube profile") {
     val profile 	= Profile.Cube
     val model 		= JenaUtils.parseFromURI(demoCubeURI)
     profile.validateExpanded(model) match {
       case Passed => info("Validates")
       case NotPassed(model) => fail("Does not validate " + model)
     }
   }

  it("Should validate computex demo with RDF Data Cube profile") {
     val profile 	= Profile.Cube
     val model 		= JenaUtils.parseFromURI(demoComputexURI)
     profile.validateExpanded(model) match {
       case Passed => info("Validates")
       case NotPassed(model) => fail("Does not validate " + model)
     }
   }
  
  it("Should validate cube demo with Computex profile") {
     val profile 	= Profile.Computex
     val model 		= JenaUtils.parseFromURI(demoCubeURI)
     profile.validateExpanded(model) match {
       case Passed => info("Validates")
       case NotPassed(model) => fail("Does not validate " + model)
     }
   }

  it("Should validate computex demo with Computex profile") {
     val profile 	= Profile.Computex
     val model 		= JenaUtils.parseFromURI(demoComputexURI)
     profile.validateExpanded(model) match {
       case Passed => info("Validates")
       case NotPassed(model) => fail("Does not validate " + model)
     }
   }
  
  it("Should parse and generate the same cube profile") {
     val computexProfile = ConfigUtils.getName(conf, "cubeProfile")
     val contents 		= Source.fromFile(computexProfile).mkString
     val model			= parseModel(contents,"",Turtle).get
     val profile 	= Profile.Cube
     val modelGenerated = ProfileParser.toModel(profile)
     if (model.isIsomorphicWith(modelGenerated)) 
       info("Models are isomorphic")
     else {
       info("Model1: " + model2Str(model))
       info("Model2: " + model2Str(modelGenerated))
       fail("Models are not isomorphic.")
     } 
   }

    it("Should parse and generate the same computex profile") {
     val computexProfile = ConfigUtils.getName(conf, "computexProfile")
     val contents 		= Source.fromFile(computexProfile).mkString
     val model			= parseModel(contents,"",Turtle).get
     val profile 	= Profile.Computex
     val modelGenerated = ProfileParser.toModel(profile)
     if (model.isIsomorphicWith(modelGenerated)) 
       info("Models are isomorphic")
     else {
       info("Model1: " + model2Str(model))
       info("ProfileGenerated: " + profile.toString)
       info("Model2: " + model2Str(modelGenerated))
       fail("Models are not isomorphic.")
     } 
   }

  
  }  
}
