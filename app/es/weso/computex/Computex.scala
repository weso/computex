package es.weso.computex

import scala.collection.JavaConversions._
import java.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.ontology.OntModelSpec._
import com.hp.hpl.jena.rdf.model.ModelExtract
import com.hp.hpl.jena.rdf.model.StatementBoundaryBase
import org.slf4j._
import com.hp.hpl.jena.rdf.model._
import org.rogach.scallop._
import com.typesafe.config._
import es.weso.computex.entities.ErrorMessage
import es.weso.utils.JenaUtils
import es.weso.utils.JenaUtils.TURTLE
import com.hp.hpl.jena.Jena
import jena.turtle

class Computex {
  
  def computex(indexDataURI: String,
    ontologyURI: String,
    validationDir: String): Model = {
    println("Computex: Compute and Validate index data")

    val model = loadData(ontologyURI, indexDataURI)

    validate(model, validationDir)
  }

  def loadData(ontologyURI: String,
    indexDataURI: String): Model = {
    val model = ModelFactory.createDefaultModel
    loadTurtle(model, ontologyURI)
    loadTurtle(model, indexDataURI)
  }

  def validate(model: Model,
    validationDir: String): Model = {
    val reportModel = ModelFactory.createDefaultModel
    val qs = readQueries(validationDir)
    for (q <- qs) {
      val newModel = executeQuery(model, q)
      reportModel.add(newModel)
    }
    reportModel
  }

  def readQueries(dirName: String): Array[Query] = {
    val dir = new File(dirName)
    if (dir == null || dir.listFiles == null)
      throw new IOException(s"Directory: ${dirName} not accessible")
    else {
      for (file <- dir.listFiles if file.getName endsWith ".sparql") yield {
        val contents = scala.io.Source.fromFile(file).mkString;
        QueryFactory.create(contents)
      }
    }
  }

  def loadTurtle(model: Model, fileName: String) = {

    model.read(fileName, "", TURTLE)
  }

  def executeQuery(model: Model, query: Query): Model = {
    val resultModel = ModelFactory.createDefaultModel
    val qexec = QueryExecutionFactory.create(query, model)
    qexec.execConstruct(resultModel)
    resultModel
  }
}