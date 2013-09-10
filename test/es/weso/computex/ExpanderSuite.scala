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
import com.hp.hpl.jena.update.UpdateFactory

class ExpanderSuite 
	extends FunSpec 
	with SparqlSuite 
	with ShouldMatchers {
 describe("Expander object") {
   it("Should expand a model") {
     val strModel =
    		"""|@prefix qb:  <http://purl.org/linked-data/cube#> .
               |@prefix : <http://example.org#> .
               |:a qb:observation :o1 .""".stripMargin
     
     val query = 
       		"""|PREFIX qb:             <http://purl.org/linked-data/cube#>
    		   |INSERT {
     		   |?o a qb:Observation .
     		   |} WHERE { [] qb:observation ?o . }""".stripMargin
    		 		
     val strExpected =
       		"""|@prefix qb:  <http://purl.org/linked-data/cube#> .
               |@prefix : <http://example.org#> .
               |:o1 a qb:Observation .
       		   |:a qb:observation :o1 .""".stripMargin
                   
     val model 		= parseModel(strModel)
     val expected	= parseModel(strExpected)
     val updateReq  = UpdateFactory.create(query)
     val expander 	= Expander(updateReq,"expander")
     expander.expand(model) match {
       case None => fail("Empty model expanded")
       case Some(expanded) => 
         shouldBeIsomorphicWith(expanded,expected)
     }
     
   }

 }
 
 def shouldBeIsomorphicWith(model1 : Model, model2: Model) : Unit = {
   if (model1.isIsomorphicWith(model2)) 
    info("Models are isomorphic as expected")
   else {
    info("Model1:\n " + model2Str(model1))
    info("Model2:\n " + model2Str(model2))
    fail("Models are not isomorphic")
   }
 }

}
