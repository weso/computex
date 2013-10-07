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

class ProfileSuite 
	extends FunSpec 
	with ShouldMatchers {

  val conf : Config = ConfigFactory.load()
  
  val demoCubeURI			= ConfigUtils.getName(conf,"demoCubeURI")
  val demoComputexURI		= ConfigUtils.getName(conf,"demoURI")
  val demoComputexAbbrURI	= ConfigUtils.getName(conf,"demoAbbrURI")
  val ontologyURI			= ConfigUtils.getName(conf,"ontologyURI")
  
  val cubeProfile 		= ConfigUtils.getName(conf, "cubeProfile")
  val computexProfile 	= ConfigUtils.getName(conf, "computexProfile")

  describe("Profile") {

    it("Should compute generated example with 2 countries, 1 indicator and 1 year") {
      
      val computex = Profile.Computex
      val model = Generator(2,1,1).model
      val computed = ModelFactory.createDefaultModel
      computed.add(model)
      computed.setNsPrefixes(model.getNsPrefixMap())
      computex.compute(computed)
      info("Model 1: " + model2Str(model))
      info("Model computed: " + model2Str(computed))
      model2File(computed,"kiko.ttl","TURTLE")
    }

  
  }  
}
