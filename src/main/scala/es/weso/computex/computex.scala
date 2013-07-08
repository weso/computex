package es.weso.computex

import scala.collection.JavaConversions._
import java.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.ontology.OntModelSpec._
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import org.slf4j._
import com.hp.hpl.jena.rdf.model._
import org.rogach.scallop._
import com.typesafe.config._
import scala.io.Source._

class Computex {

 def computex(indexDataURI: String,
              ontologyURI: String,
              validationDir: String,
              computationDir: String,
              cubeDataDir: String,
              closureFile: String,
              flattenFile: String,
              findStepsQuery: String
             ) = {
  try {

     println("Computex: Compute and Validate index data")

     val model = loadData(ontologyURI,indexDataURI)
     val expanded = expandCube(model,closureFile,flattenFile)
     

     val validationModel = validate(expanded,validationDir)
     if (validationModel.size == 0) {
       println("No errors");
       val computexModel = expandComputex(model,computationDir,findStepsQuery)
       println("Computed Model: ")
       computexModel.write(System.out,"TURTLE")
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

 def expandCube(model: Model,
            closureFile: String,
            flattenFile: String
           ): Model = {
    val closure = fromFile(closureFile).mkString
    val flatten = fromFile(flattenFile).mkString
    val ds : Dataset = DatasetFactory.create(model)
    val graphStore : GraphStore = GraphStoreFactory.create(ds) 
    UpdateAction.parseExecute(closure, graphStore)
    UpdateAction.parseExecute(flatten, graphStore)
    val result : Model = ModelFactory.createModelForGraph( graphStore.getDefaultGraph() )
    result.setNsPrefixes(model)
    result
 }

  
  def expandComputex(
      model: Model,
      computationDir: String,
      findStepsQuery: String
    ) : Model = {
    val ds : Dataset = DatasetFactory.create(model)
    val graphStore : GraphStore = GraphStoreFactory.create(ds)
    val steps = getSteps(model,findStepsQuery)
    for (step <- steps) {
      val contents = fromFile(computationDir + step + ".sparql").mkString
      UpdateAction.parseExecute(contents, graphStore)
    }
    val result : Model = ModelFactory.createModelForGraph( graphStore.getDefaultGraph() )
    result.setNsPrefixes(model)
    result
 }

  def getSteps(model:Model,
      findStepsQuery: String
      ) : List[String] = {
    val file = new File(findStepsQuery)
    val query = readQuery(file)
    val qexec = QueryExecutionFactory.create(query, model)
    try {
    	val result = qexec.execSelect()
    	collect("stepQuery",result)
    } finally {
      qexec.close
    }
  }
  
  def collect(varName: String, result: ResultSet) : List[String] = {
    val ls = result.toList
    ls.map(r => r.getLiteral(varName).getString)
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

 /*
 def compute(model: Model, 
            computationDir: String): Model = {
 
  val computedModel = ModelFactory.createDefaultModel
  val qs = readQueries(computationDir)
  for (q <- qs) {
     val newModel = executeQuery(model,q)
     computedModel.add(newModel)
  }
  computedModel
 } */

 def readQueries(dirName : String) : Array[Query] = {
  val dir = new File(dirName)
  if (dir == null || dir.listFiles == null) 
     throw new IOException("Directory: " + dirName + " not accessible")
  else {
    for (file <- dir.listFiles if file.getName endsWith ".sparql") yield {
      readQuery(file) 
    }  
   }
 }  
 
 def readQuery(file: File) : Query = {
   val contents = scala.io.Source.fromFile(file).mkString ;
   QueryFactory.create(contents)
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