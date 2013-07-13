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

class ComputexSuite extends FunSpec with SparqlSuite with ShouldMatchers {

  val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val testDataDir = conf.getString("testDataDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val demoURI = conf.getString("demoURI")
  val demoAbbrURI = conf.getString("demoAbbrURI")
  val closureFile = conf.getString("closureFile")
  val flattenFile = conf.getString("flattenFile")
  val findStepsQuery = conf.getString("findStepsQuery")

  
  val cex = new Computex
  
  val queryErrorMsg : String = 
   """PREFIX cex: <http://purl.org/weso/ontology/computex#>
   SELECT ?msg WHERE { 
    ?e a cex:Error ; cex:msg ?msg . 
   }"""

    
  describe("No errors in demo data") {
	  val model = cex.loadData(ontologyURI,demoURI)
      passDir(model,validationDir)
  }

  describe("The expanded model using computex") {
	val model = cex.loadData(ontologyURI,demoAbbrURI)
	val expandedCube = cex.expandCube(model,closureFile,flattenFile)
	val expandedCex = cex.expandComputex(expandedCube,computationDir,findStepsQuery)
    
    describe("Should pass all validation tests from Computex") {
	  passDir(model,validationDir)
    } 

	it("Should contain 6 imputed values using Mean") {
	  val rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	  val mean = model.createResource("http://purl.org/weso/ontology/computex#Mean")
      val ls = model.listResourcesWithProperty(rdfType,mean)
	  ls.toList.size should be(6)
    } 

    it("Should contain 1 imputed values using AvgGrowth") {
	  val rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	  val avgGrowth = model.createResource("http://purl.org/weso/ontology/computex#AverageGrowth")
      val ls = model.listResourcesWithProperty(rdfType,avgGrowth)
	  ls.toList.size should be(1)
    } 

   it("Should contain 36 filtered values") {
	  val rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	  val filter  = model.createResource("http://purl.org/weso/ontology/computex#Filter")
      val ls = model.listResourcesWithProperty(rdfType,filter)
	  ls.toList.size should be(36)
    } 

   it("Should contain 36 normalized values") {
	  val rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	  val normalize = model.createResource("http://purl.org/weso/ontology/computex#Normalize")
      val ls = model.listResourcesWithProperty(rdfType,normalize)
	  ls.toList.size should be(36)
    } 

   it("Should contain 18 adjusted values") {
	  val rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	  val adjusted = model.createResource("http://purl.org/weso/ontology/computex#Adjust")
      val ls = model.listResourcesWithProperty(rdfType,adjusted)
	  ls.toList.size should be(18)
    } 

    it("Should contain 18 weighted values") {
	  val rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	  val weighted = model.createResource("http://purl.org/weso/ontology/computex#Weighted")
      val ls = model.listResourcesWithProperty(rdfType,weighted)
	  ls.toList.size should be(18)
    } 

    it("Should contain 18 grouped mean values") {
	  val rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	  val grouped = model.createResource("http://purl.org/weso/ontology/computex#GroupMean")
      val ls = model.listResourcesWithProperty(rdfType,grouped)
	  ls.toList.size should be(18)
    } 

  }
  
  
  describe("The validation process") {
    
  it("Should raise error if obs has no value") {
   val model = loadExample("obs_noValue.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have value")
  }

 it("Should raise error if an observation has 2 values") {
   val model = loadExample("obs2Values.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation has two different values")
  }

 it("Should raise error if an observation with Missing status has value") {
   val model = loadExample("missingObsNoValues.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation with Status obsStatus-M (Missing) should not have value")
  }

 it("Checks an Observation that does not have sheet-type") {
   val model = loadExample("obs_noSheet-type.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have sheet-type")
  }

  it("Should raise error with bad normalized") {
   val model = loadExample("badNormalized.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Normalized value does not match computed z-score")
  }

  it("Should raise error with bad normalized low") {
   val model = loadExample("badNormalizedLow.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Normalized value does not match computed z-score")
  }

  it("Should raise No error with good normalized") {
   val model = loadExample("goodNormalizedHigh.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  it("Should raise No error with good normalized low") {
   val model = loadExample("goodNormalizedLow.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  it("Should raise Error with bad incremented") {
   val model = loadExample("badIncremented.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Adjusted value does not match")
  }

  it("Should raise No error with good incremented") {
   val model = loadExample("goodIncremented.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  it("Should raise Error with bad incremented no value source") {
   val model = loadExample("badIncrementedNoValueSource.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Source observation does not have value in adjusted computation")
  }

  it("Should raise Error with bad mean") {
   val model = loadExample("badMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Mean value does not match")
  }

  it("Should raise No Error with good mean") {
   val model = loadExample("goodMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  it("Should raise Error with bad mean no source") {
   val model = loadExample("badMeanNoSource.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have value in mean")
  }

  it("Should raise Error with bad copy") {
   val model = loadExample("badCopy.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Copy value does not match")
  }

  it("Should raise No Error with good copy") {
   val model = loadExample("goodCopy.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }

  it("Should raise Error with bad copy no source") {
   val model = loadExample("badCopyNoSource.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Observation does not have value in copy")
  }

  it("Should raise Error with bad weighted mean") {
   val model = loadExample("badWeightedMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Weighted Mean does not match")
  }

  it("Should raise No Error with good weighted mean") {
   val model = loadExample("goodWeightedMean.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size === 0)
  }
  
  it("Should raise Error with bad ranking") {
   val model = loadExample("badRanking.ttl") 
   val reportModel = cex.validate(model,validationDir)  
   assert(reportModel.size > 0)
   assertMsgError(reportModel, "Ranking value does not match")
  }

  it("Should raise No Error with good ranking") {
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