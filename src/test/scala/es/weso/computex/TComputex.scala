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

 test("Observation does not have sheet-type") {
   val model = loadExample("obs_noSheet-type.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have sheet-type")
  }

  test("Error with bad normalized") {
   val model = loadExample("badNormalized.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Normalized value does not match computed z-score")
  }

  test("Error with bad normalized low") {
   val model = loadExample("badNormalizedLow.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Normalized value does not match computed z-score")
  }

  test("No error with good normalized") {
   val model = loadExample("goodNormalizedHigh.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  test("No error with good normalized low") {
   val model = loadExample("goodNormalizedLow.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  test("Error with bad incremented") {
   val model = loadExample("badIncremented.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Incremented value does not match")
  }

  test("No error with good incremented") {
   val model = loadExample("goodIncremented.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  test("Error with bad incremented no value source") {
   val model = loadExample("badIncrementedNoValueSource.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Source observation does not have value in incremented computation")
  }

  test("Error with bad mean") {
   val model = loadExample("badMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Mean value does not match")
  }

  test("No Error with good mean") {
   val model = loadExample("goodMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  test("Error with bad mean no source") {
   val model = loadExample("badMeanNoSource.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have value in mean")
  }

  test("Error with bad copy") {
   val model = loadExample("badCopy.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Copy value does not match")
  }

  test("No Error with good copy") {
   val model = loadExample("goodCopy.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  test("Error with bad copy no source") {
   val model = loadExample("badCopyNoSource.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have value in copy")
  }

  test("Error with bad weighted mean") {
   val model = loadExample("badWeightedMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Weighted Mean does not match")
  }

  test("No Error with good weighted mean") {
   val model = loadExample("goodWeightedMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }
  
  test("Error with bad ranking") {
   val model = loadExample("badRanking.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Ranking value does not match")
  }

  test("No Error with good ranking") {
   val model = loadExample("goodRanking.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
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