package es.weso.computex

import scala.collection.JavaConversions._
import java.io._
import scala.io.Source._
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
import scala.io.Source
import play.api.libs.ws.WS
import java.net.URL
import com.hp.hpl.jena.update.GraphStore
import com.hp.hpl.jena.update.GraphStore
import com.hp.hpl.jena.update.GraphStoreFactory
import com.hp.hpl.jena.update.UpdateAction

case class Computex(val ontologyURI: String, val validationDir: String,
  val closureFile: String, val flattenFile: String) {

  def computex(indexDataInputStream: InputStream): Model = {
    println("Computex: Compute and Validate index data")

    val model = loadData(ontologyURI, indexDataInputStream)
    val expandedModel = expandCube(model)
    validate(expandedModel, validationDir)
  }

  def loadData(ontologyURI: String,
    indexDataInputStream: InputStream): Model = {
    val model = ModelFactory.createDefaultModel
    loadTurtle(model, Computex.loadFile(ontologyURI))
    loadTurtle(model, indexDataInputStream)
  }

  def expandCube(model: Model): Model = {
    val closure = fromFile(closureFile).mkString
    val flatten = fromFile(flattenFile).mkString
    val ds: Dataset = DatasetFactory.create(model)
    val graphStore: GraphStore = GraphStoreFactory.create(ds)
    UpdateAction.parseExecute(closure, graphStore)
    UpdateAction.parseExecute(flatten, graphStore)
    val result: Model = ModelFactory.createModelForGraph(graphStore.getDefaultGraph())
    result.setNsPrefixes(model)
    result
  }

  def validate(model: Model,
    validationDir: String): Model = runQueries(model, validationDir)

  def compute(model: Model,
    computationDir: String): Model = runQueries(model, computationDir)

  def runQueries(model: Model, dir: String): Model = {
    val currentModel = ModelFactory.createDefaultModel
    val qs = readQueries(dir)
    for (q <- qs) {
      val newModel = executeQuery(model, q)
      currentModel.add(newModel)
    }
    currentModel
  }

  def readQueries(dirName: String): Array[Query] = {
    val dir = new File(dirName)
    if (dir == null || dir.listFiles == null)
      throw new IOException(s"Directory: ${dirName} not accessible")
    else {
      for (file <- dir.listFiles if file.getName endsWith ".sparql") yield {
        val contents = fromFile(file).mkString;
        QueryFactory.create(contents)
      }
    }
  }

  def loadTurtle(model: Model, inputStream: InputStream) = {
    model.read(inputStream, "", TURTLE)
  }

  def executeQuery(model: Model, query: Query): Model = {
    val resultModel = ModelFactory.createDefaultModel
    val qexec = QueryExecutionFactory.create(query, model)
    qexec.execConstruct(resultModel)
    resultModel
  }
}

object Computex {
  def loadFile(fileName: String): InputStream = {
    val url = new URL(fileName)
    val urlCon = url.openConnection()
    urlCon.setConnectTimeout(2000)
    urlCon.setReadTimeout(1000)
    urlCon.getInputStream()
  }
}