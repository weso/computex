package es.weso.validex

import scala.collection.JavaConversions._
import java.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.ontology.OntModelSpec._
import org.slf4j._
import com.hp.hpl.jena.rdf.model._

object Application extends App {

  val logger = LoggerFactory.getLogger("Application")
  val validationDirName = "../ontology/validation"
  val ontologyFileName  = "file:../ontology/ontology.ttl"
  val indexDataFileName = "file:../ontology/examples/exampleWithErrors.ttl"

    try {
     println("Validex: Validating index data")
     printCWD()
     val model = ModelFactory.createDefaultModel
     loadTurtle(model,ontologyFileName)
     println("Ontology loaded. Size of ontology = " + model.size)
     loadTurtle(model,indexDataFileName)
     println("Index data loaded. Size of model = " + model.size)

     val reportModel = ModelFactory.createDefaultModel

     val qs = readValidationQueries
     for (q <- qs) {
       executeQuery(model,q,reportModel)
     }
    
     println("Report Model: ")
     reportModel.write(System.out,"TURTLE")
    
    } catch {
      case ex : Exception => println("Exception: " + ex)
    }
    
    

def printCWD() {
  val cwd = new File(".")
  println("CWD = " + cwd.getAbsolutePath())
}
   
 def readValidationQueries() : Array[Query] = {
  val validationDir = new File(validationDirName)
  if (validationDir == null || validationDir.listFiles == null) 
     throw new IOException("Validation directory: " + validationDirName + " not accessible")
  else {
    for (file <- validationDir.listFiles if file.getName endsWith ".sparql") yield {
      val contents = scala.io.Source.fromFile(file).mkString ;
      QueryFactory.create(contents) 
    }  
   }
 }  

 def loadTurtle(model: Model, fileName: String) = {
  model.read(fileName, "","TURTLE")
 }
   
 def executeQuery(model:Model, query: Query, reportModel: Model) = {
  val qexec = QueryExecutionFactory.create(query, model)
  qexec.execConstruct(reportModel)
 }
}