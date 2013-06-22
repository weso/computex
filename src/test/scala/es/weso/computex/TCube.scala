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
import java.io.IOException
import java.io.File

class CubeSuite extends FunSpec with ShouldMatchers {

  val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val cubeDataDir = conf.getString("cubeDataDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val indexDataURI_ok = conf.getString("indexDataURI_ok")
  
  
  val cex = new Computex

  val PREFIXES =
"""
PREFIX rdf:            <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:           <http://www.w3.org/2000/01/rdf-schema#>
PREFIX skos:           <http://www.w3.org/2004/02/skos/core#>
PREFIX qb:             <http://purl.org/linked-data/cube#>
PREFIX xsd:            <http://www.w3.org/2001/XMLSchema#>
PREFIX owl:            <http://www.w3.org/2002/07/owl#>
PREFIX eg:             <http://example.com/abbrv-cube/>
"""
    
  describe("The example") {
    describe("should pass all the RDF Data Cube integrity tests") {
      val model_ok = cex.loadData(ontologyURI,indexDataURI_ok)

      val dir = new File(cubeDataDir)
	  if (dir == null || dir.listFiles == null) 
		  throw new IOException("Directory: " + cubeDataDir + " not accessible")
	  else {
		  for (file <- dir.listFiles ;
		       if file.getName startsWith "integrity" ;
		       if file.getName endsWith ".sparql") {
		    val name = file.getName.dropRight(7) // remove ".sparql" = 7 chars 
		    val contents = PREFIXES + scala.io.Source.fromFile(file).mkString ;
		    val query = QueryFactory.create(contents) 
		    pass(name,query,model_ok)
		  }
	  }
    }
  }

  def pass(name : String, query: Query, model: Model) = {
    it("should pass integrity check: " + name) {
     val qe = QueryExecutionFactory.create(query,model)
     try {
       val reportModel = ModelFactory.createDefaultModel
       qe.execConstruct(reportModel)
       reportModel.size should be(0);
     } finally {
       qe.close();
    }
   }
  }

  def notPass(name:String, query: Query, model: Model) = {
    it("should not pass integrity check: " + name) {
     val qe = QueryExecutionFactory.create(query,model)
     try {
       val reportModel = ModelFactory.createDefaultModel
       qe.execConstruct(reportModel)
       reportModel.size should be(0);
     } finally {
       qe.close();
     }
  }
 }

}