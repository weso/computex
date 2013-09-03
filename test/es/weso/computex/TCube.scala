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

class CubeSuite extends 
			FunSpec with 
			SparqlSuite with 
			ShouldMatchers {

  val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val cubeDataDir = conf.getString("cubeDataDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val demoURI = conf.getString("demoURI")
  val demoAbbrURI = conf.getString("demoAbbrURI")
  val demoCubeUri = conf.getString("demoCubeURI")
  val closureFile = conf.getString("closureFile")
  val flattenFile = conf.getString("flattenFile")
  
  
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

  describe("The Cube example") {

   describe("should pass all the RDF Data Cube integrity tests") {
      val model = cex.loadData(demoCubeUri)
      val expanded = cex.expandCube(model,closureFile,flattenFile)
      passDir(model,cubeDataDir)
    }
  }

  describe("The Computex Abbr example") {

    describe("should pass all the RDF Data Cube integrity tests") {
      val model = cex.loadData(ontologyURI,demoAbbrURI)
      val expanded = cex.expandCube(model,closureFile,flattenFile)
      cex.validate(expanded,cubeDataDir)
      passDir(expanded,cubeDataDir)
    }
  }
  
  describe("The Computex example") {

    describe("should pass all the RDF Data Cube integrity tests") {
      val model = cex.loadData(ontologyURI,demoURI)
      val expanded = cex.expandCube(model,closureFile,flattenFile)
      passDir(expanded,cubeDataDir)
    }
    
  }
  

}