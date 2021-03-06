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
import es.weso.utils.ConfigUtils
import es.weso.utils.JenaUtils
import scala.io.Source
import java.net.URI
import es.weso.computex.SparqlSuite

class ValidatorSuite 
	extends FunSpec 
	with SparqlSuite 
	with ShouldMatchers {

  val conf : Config = ConfigFactory.load()
  val demoCubeURI	= ConfigUtils.getName(conf,"demoCubeURI")
  val cubeDataDir   = ConfigUtils.getName(conf,"cubeDataDir")
  
  describe("Validator object") {
   it("Should not validate a query without values for observations") {
     val queryStr = """|prefix cex: <http://purl.org/weso/ontology/computex#>
                    |prefix : <http://example.org#>
                    |
    		 	    |CONSTRUCT { [ a cex:error ; cex:msg "Obs has no value" ] } 
                    |WHERE {
    		 	    |  ?obs a :Observation .
    		 		|  FILTER NOT EXISTS { ?obs cex:value ?value }
    		 		|}""".stripMargin
    		 		
     val data = """|@prefix cex: <http://purl.org/weso/ontology/computex#> .
                   |@prefix : <http://example.org#> .
                   |:o1 a :Observation .
                   |""".stripMargin
                   
     val model 		= parseFromString(data)
     val validator 	= Validator(queryStr)
     shouldNotPassReport(validator.validate(model))
   }

    it("Should pass a query with values for observations") {
     val queryStr = """|prefix cex: <http://purl.org/weso/ontology/computex#>
                    |prefix : <http://example.org#>
                    |
    		 	    |CONSTRUCT { [ a cex:error ; cex:msg "Obs has no value" ] } 
                    |WHERE {
    		 	    |  ?obs a :Observation .
    		 		|  FILTER NOT EXISTS { ?obs cex:value ?value }
    		 		|}""".stripMargin
    		 		
     val data = """|@prefix cex: <http://purl.org/weso/ontology/computex#> .
                   |@prefix : <http://example.org#> .
                   |:o1 a :Observation ;
       			   |    cex:value 1 .
                   |""".stripMargin
                   
     val model 		= parseFromString(data)
     val validator 	= Validator(queryStr)
     shouldPassReport(validator.validate(model))
   }
  
    it("Should pass q13-integrity with RDF Data Cube demo.ttl") {
      val model = JenaUtils.parseFromURI(demoCubeURI)
      val queryStr = Source.fromFile(cubeDataDir + "q13-integrity.sparql").mkString
      val validator = Validator(queryStr)
      shouldPassReport(validator.validate(model))
    }
  
  }
 
  
  
 def shouldPassReport(report: ValidationReport[Validator,(Validator,Model)]) : Unit = {
     report match {
       case Passed(_) => info("Validates without errors")
       case NotPassed((v,error)) => fail("Does not validate. Error: \n" + model2Str(error))
     }
 }

 def shouldNotPassReport(report: ValidationReport[Validator,(Validator,Model)]) : Unit = {
     report match {
       case Passed(_) => fail("Validates without errors. But expected not to pass")
       case NotPassed(_) => info("Does not validate as expected")
     }
 }

}
