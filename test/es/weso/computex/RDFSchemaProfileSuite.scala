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

class RDFSchemaProfileSuite 
	extends FunSpec 
	with ShouldMatchers {

  describe("RDF Schema profile") {
   it("Should parse RDF Schema Profile") {
     
     val profile = Profile.RDFSchema
     profile.allValidators.size should be(1)
   }

   it("Should infer modus ponens basic") {
     val profile = Profile.RDFSchema
     val str = """|@prefix : <http://example.org#> .
                  |@prefix rdf:          <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    		 	  |@prefix rdfs:         <http://www.w3.org/2000/01/rdf-schema#> .
                  |:a rdf:type :A .
                  |:A rdfs:subclassOf :B .
                  """.stripMargin
     val model = JenaUtils.parseFromString(str)
     val (vr,_) = profile.validate(model)             
     vr match {
       case Passed(_) => info("Passed RDF schema validation")
       case NotPassed((vs,verr)) => fail("Not passed. Failed " + verr.size + " validators")
     }
   }
  }

}
