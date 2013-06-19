package es.weso.computex

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.util.FileManager;
import scala.collection.JavaConversions._

class ComputexSuite extends FunSuite with ShouldMatchers {

  val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val testDataDir = conf.getString("testDataDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val indexDataURI_ok = conf.getString("indexDataURI_ok")
  
  
  val cex = new Computex
  
  val queryErrorMsg : String = 
   """PREFIX cex: <http://purl.org/weso/ontology/computex#>
   SELECT ?msg WHERE { 
    ?e a cex:Error ; cex:msg ?msg . 
   }"""

  test("No errors in good index data") {
	  val model_ok = cex.loadData(ontologyURI,indexDataURI_ok)
	  val errorModel = cex.validate(model_ok,validationDir) 
	  errorModel.size should be(0)
  }

  test("Error if obs has no value") {
   val model = loadExample("obs_noValue.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have value")
  }

 test("Error if an observation has 2 values") {
   val model = loadExample("obs2Values.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation has two different values")
  }

   test("Error if an observation with Missing status has value") {
   val model = loadExample("missingObsNoValues.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation with Status obsStatus-M (Missing) should not have value")
  }

 def loadExample(name: String) : Model = {
   val model = ModelFactory.createDefaultModel()
   cex.loadTurtle(model,ontologyURI)
   FileManager.get.readModel(model, testDataDir + name)
   model
 }
 
 def assertMsgError(model : Model, msg: String) = {
   val results = 
		   QueryExecutionFactory.create(queryErrorMsg, model).execSelect;
   val msgs = (for { r <- results } 
                yield r.get("?msg").asLiteral.getString
              ).toList
    msgs should contain(msg)
  }
 
 

}