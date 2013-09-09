package es.weso.computex

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

class ValidatorSuite 
	extends FunSpec 
	with SparqlSuite 
	with ShouldMatchers {
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
                   
     val model 		= parseModel(data).get
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
                   
     val model 		= parseModel(data).get
     val validator 	= Validator(queryStr)
     shouldPassReport(validator.validate(model))
   }
}
 
  describe("Validators") {
   it("Should not validate a query without values for observations") {
   }
  }
 
 def shouldPassReport(report: ValidationReport[Model,Validator,Validator]) : Unit = {
     report match {
       case Passed(_) => info("Validates without errors")
       case NotPassed(error,_) => fail("Does not validate. Error: \n" + model2Str(error))
     }
 }

 def shouldNotPassReport(report: ValidationReport[Model,Validator,Validator]) : Unit = {
     report match {
       case Passed(_) => fail("Validates without errors. But expected not to pass")
       case NotPassed(error,_) => info("Does not validate as expected")
     }
 }

}
