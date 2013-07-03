package es.weso.computex

import scala.collection.JavaConversions._
import java.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.ontology.OntModelSpec._
import org.slf4j._
import com.hp.hpl.jena.rdf.model._
import org.rogach.scallop._
import com.typesafe.config._

class Computex {
 def computex(indexDataURI: String,
             ontologyURI: String,
             validationDir: String,
             computationDir: String) = {
  try {

     println("Computex: Compute and Validate index data")

     val model = loadData(ontologyURI,indexDataURI)

     val validationModel = validate(model,validationDir)
     if (validationModel.size == 0) {
       println("No errors");
       val computedModel = compute(model,computationDir)
       println("Computed Model: ")
       computedModel.write(System.out,"TURTLE")
     } else {
       println("Validation Model: ")
       validationModel.write(System.out,"TURTLE")
     }
    } catch {
      case ex : Exception => println("Exception: " + ex)
    }
  }    

 def loadData(dataURI: String) : Model = {
  val model = ModelFactory.createDefaultModel
  loadTurtle(model,dataURI)
 }

 def loadData(ontologyURI: String, 
              indexDataURI: String) : Model = {
  val model = ModelFactory.createDefaultModel
  loadTurtle(model,ontologyURI)
  loadTurtle(model,indexDataURI)
 }
 
 def validate(model: Model, 
              validationDir: String): Model = {
     val reportModel = ModelFactory.createDefaultModel
     val qs = readQueries(validationDir)
     for (q <- qs) {
       val newModel = executeQuery(model,q)
       reportModel.add(newModel)
     }
     reportModel
 }

 def compute(model: Model, 
            computationDir: String): Model = {
 
  val computedModel = ModelFactory.createDefaultModel
  val qs = readQueries(computationDir)
  for (q <- qs) {
     val newModel = executeQuery(model,q)
     computedModel.add(newModel)
  }
  computedModel
 }

 def readQueries(dirName : String) : Array[Query] = {
  val dir = new File(dirName)
  if (dir == null || dir.listFiles == null) 
     throw new IOException("Directory: " + dirName + " not accessible")
  else {
    for (file <- dir.listFiles if file.getName endsWith ".sparql") yield {
      val contents = scala.io.Source.fromFile(file).mkString ;
      QueryFactory.create(contents) 
    }  
   }
 }  

 def loadTurtle(model: Model, fileName: String) = {
  model.read(fileName, "","TURTLE")
 }
   
 def executeQuery(model:Model, query: Query) : Model = {
  val resultModel = ModelFactory.createDefaultModel
  val qexec = QueryExecutionFactory.create(query, model)
  qexec.execConstruct(resultModel)
  resultModel
 }
}