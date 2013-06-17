package es.weso.computex

import org.scalatest._
import com.typesafe.config._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.rdf.model._

class ComputexSuite extends FunSuite with BeforeAndAfter {

  val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val indexDataURI_ok = conf.getString("indexDataURI_ok")
  val indexDataURI_withErrors = conf.getString("indexDataURI_withErrors")
  
  val cex = new Computex
  val model_ok = cex.loadData(ontologyURI,indexDataURI_ok)
  val model_withErrors = cex.loadData(ontologyURI,indexDataURI_withErrors)

  test("just checking true...") {
    assert(true,true)
  }
  
  test("No errors in good index data") {
   val reportModel = cex.validate(model_ok,validationDir)  
   assert(reportModel.size == 0)
  }

  test("Errors in bad index data") {
   val reportModel = cex.validate(model_withErrors,validationDir)  
   assert(reportModel.size > 0)
  }

 test("Error if an observation has 2 values") {
   val reportModel = cex.validate(model_withErrors,validationDir)
  }

}