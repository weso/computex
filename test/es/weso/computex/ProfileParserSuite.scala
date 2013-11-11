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
import es.weso.computex.profile._
import es.weso.computex.profile.VReport._
import es.weso.computex.Generator

class ProfileParserSuite 
	extends FunSpec 
	with ShouldMatchers {

  val conf : Config = ConfigFactory.load()
  
  val demoCubeURI			= ConfigUtils.getName(conf,"demoCubeURI")
  val demoComputexURI		= ConfigUtils.getName(conf,"demoURI")
  val demoComputexAbbrURI	= ConfigUtils.getName(conf,"demoAbbrURI")
  val ontologyURI			= ConfigUtils.getName(conf,"ontologyURI")
  
  val cubeProfile 		= ConfigUtils.getName(conf, "cubeProfile")
  val computexProfile 	= ConfigUtils.getName(conf, "computexProfile")

  describe("Profile Parser") {
   it("Should obtain a list of validators for RDF Data Cube") {
     val profile = Profile.Cube
     profile.validators.length should be(23)
   }

   it("Should obtain a list of validators for Computex") {
     val profile = Profile.Computex
     profile.validators.length should be(14)
   }

   it("Should obtain a list of all validators for Computex") {
     val profile = Profile.Computex
     profile.allValidators.length should be(37)
   }

  it("Should obtain a list of computeSteps for RDF Data Cube profile") {
     val profile = Profile.Cube
     profile.computeSteps.length should be(10)
   }

  it("Should obtain a list of computeSteps for Computex profile") {
     val profile = Profile.Computex
     profile.computeSteps.length should be(12)
   }

  it("Should validate demo of RDF Data Cube profile") {
     val profile 		= Profile.Cube
     val demoCube 		= JenaUtils.parseFromURI(demoCubeURI)
     profile.validate(demoCube) match {
       case (Passed(_),_) => info("Validates")
       case vr@(NotPassed((vs,nvs)),returnedModel) => {
         fail("not passed" + nvs)
         fail("Does not validate " + VReport.show(vr._1))
       }
     }
   }

  it("Should validate computex demo with RDF Data Cube profile") {
     val profile 	= Profile.Cube
     val model 		= JenaUtils.parseFromURI(demoComputexAbbrURI)
     val ontology 	= JenaUtils.parseFromURI(ontologyURI)
     // We need to manually merge because if we use Cube Profile, 
     // the validator doesn't know about Computex ontology which 
     // is included when we use Computex profile
     model.add(ontology)  
     profile.validate(model) match {
       case (Passed(_),_) => info("Validates")
       case vr@(NotPassed(_),returnedModel) => {
         //JenaUtils.model2File(returnedModel,"returned.ttl")
         fail("Does not validate " + VReport.show(vr._1))
       }
     }
   }
  
  // Cube demo contains observations without cex:value
  it("Should not validate cube demo with Computex profile") {
     val profile 	= Profile.Computex
     val model 		= JenaUtils.parseFromURI(demoCubeURI)
     profile.validate(model) match {
       case (Passed(_),_) => 
         fail("Validates but was expected to fail")
       case vr@(NotPassed(_),_) => 
         info("Does not validate as expected")
       
     }
   }

  ignore("Should validate computex demo with Computex profile") {
     val profile 	= Profile.Computex
     val model 		= JenaUtils.parseFromURI(demoComputexURI)
     profile.validate(model) match {
       case (Passed(_),_) => info("Validates")
       case vr@(NotPassed(_),returnedModel) => 
         JenaUtils.model2File(returnedModel,"returned.ttl")
         // info("Error model:" + model2Str(model))
         fail("Does not validate " + VReport.show(vr._1, false))
     }
   }

  ignore("Should expand a generated model with Computex profile") {
     val profile 	= Profile.Computex
     val model 		= Generator(2,1,1).model
     val expanded   = profile.expand(model)
     expanded.size should be(10)
   }

   it("Should validate computex demo generated randomly with Computex profile") {
     val profile 	= Profile.Computex
     val model 		= Generator(2,1,1).model
     profile.validate(model,false) match {
       case (Passed(_),_) => info("Validates")
       case vr@(NotPassed(e),returnedModel) => 
         // JenaUtils.model2File(returnedModel,"returned.ttl")
         // info("Error model:" + model2Str(e))
         fail("Does not validate " + VReport.show(vr._1, false))
     }
   }

  it("Should parse and generate the same cube profile") {
     val computexProfile = ConfigUtils.getName(conf, "cubeProfile")
     val contents 		= Source.fromFile(computexProfile).mkString
     val model			= parseFromString(contents,"",Turtle)
     val profile 	= Profile.Cube
     val modelGenerated = ProfileParser.toModel(profile)
     if (model.isIsomorphicWith(modelGenerated)) 
       info("Models are isomorphic")
     else {
       // info("Model1: " + model2Str(model))
       info("Model2: " + model2Str(modelGenerated))
       fail("Models are not isomorphic.")
     } 
   }

    it("Should parse and generate the same computex profile") {
     val computexProfile = ConfigUtils.getName(conf, "computexProfile")
     val contents 		= Source.fromFile(computexProfile).mkString
     val model			= parseFromString(contents,"",Turtle)
     val profile 		= Profile.Computex
     val modelGenerated = ProfileParser.toModel(profile)
     if (model.isIsomorphicWith(modelGenerated)) 
       info("Models are isomorphic")
     else {
       info("Model1: " + model2Str(model))
       // info("ProfileGenerated: " + profile.toString)
       info("Model2: " + model2Str(modelGenerated))
       fail("Models are not isomorphic.")
     } 
   }

  
  }  
}
