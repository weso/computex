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

object Application extends App {

  val opts = new ScallopConf(args) {
    banner("""Computex. Compute and validate Statistical Index Observations
              Options: --indexURI <uri> 
              For usage see below:
           """.trim())
    val indexURI = opt[String]("indexURI")
    val version = opt[Boolean]("version", noshort = true, descr = "Print version")
    val help = opt[Boolean]("help", noshort = true, descr = "Show this message")

  }

  val logger = LoggerFactory.getLogger("Application")
  
  val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val indexDataURI = opts.indexURI.get.getOrElse(conf.getString("indexDataURI")) 

  try {
     println("Computex: Compute and Validate index data")

     val model = ModelFactory.createDefaultModel
     loadTurtle(model,ontologyURI)
     println("Ontology loaded. Size of ontology = " + model.size)
     loadTurtle(model,indexDataURI)
     println("Index data loaded. Size of model = " + model.size)

     val validationModel = ModelFactory.createDefaultModel
     val qs = readQueries(validationDir)
     for (q <- qs) {
       executeQuery(model,q,validationModel)
     }
     if (validationModel.size == 0) {
       println("No errors");
       val computedModel = ModelFactory.createDefaultModel
       val qs = readQueries(computationDir)
       for (q <- qs) {
         executeQuery(model,q,computedModel)
       }
       println("Computed Model: ")
       computedModel.write(System.out,"TURTLE")

     } else {
       println("Validation Model: ")
       validationModel.write(System.out,"TURTLE")
     }


    } catch {
      case ex : Exception => println("Exception: " + ex)
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
   
 def executeQuery(model:Model, query: Query, reportModel: Model) = {
  val qexec = QueryExecutionFactory.create(query, model)
  qexec.execConstruct(reportModel)
 }
 

}