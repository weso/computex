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
import es.weso.computex.entities.CErrorMessage
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
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.IntegrityQuery
import org.apache.commons.io.IOUtils

case class Computex(val ontologyURI: String, val validationDir: String,
  val computationDir: String, val closureFile: String, val flattenFile: String,
  val findStepsQuery: String) {
  
  def computex(message: CMessage): Array[(String, IntegrityQuery)] = {
    println("Computex: Compute and Validate index data")
    val model = loadData(ontologyURI, message)
    val expandedCube = expandCube(model)
    val expandedComputex = if (message.expand) {
      expandComputex(model)
    } else { model }
    validate(expandedComputex, validationDir)
  }

  def loadData(ontologyURI: String,
    message: CMessage): Model = {
    val model = ModelFactory.createDefaultModel

    loadModel(model, Computex.loadFile(ontologyURI))
    loadModel(model, message.contentIS, message.contentFormat)
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

  def expandComputex(model: Model): Model = {
    val ds: Dataset = DatasetFactory.create(model)
    val graphStore: GraphStore = GraphStoreFactory.create(ds)
    val steps = getSteps(model)
    for (step <- steps) {
      val Pattern = s"(q.*-${step}-.*).sparql".r
      new java.io.File(computationDir).listFiles match {
        case Pattern(file)=>
           val contents = fromFile(file).mkString
           UpdateAction.parseExecute(contents, graphStore)
      }
    }
    val result: Model = ModelFactory.createModelForGraph(graphStore.getDefaultGraph())
    result.setNsPrefixes(model)
    result
  }

  def getSteps(model: Model): List[String] = {
    val file = new File(findStepsQuery)
    val query = readQuery(file)
    val qexec = QueryExecutionFactory.create(query, model)
    try {
      val result = qexec.execSelect()
      collect("stepQuery", result)
    } finally {
      qexec.close
    }
  }

  def collect(varName: String, result: ResultSet): List[String] = {
    val ls = result.toList
    ls.map(r => r.getLiteral(varName).getString)
  }

  def validate(model: Model,
    validationDir: String): Array[(String, IntegrityQuery)] = {
    val iQueries: Array[(String, IntegrityQuery)] = for {
      q <- readQueries(validationDir)
      currentModel = executeQuery(model, q._2)
    } yield {
      val iQuery = Parser.parse(q, currentModel)
      (iQuery.query._1, iQuery)
    }
    iQueries
  }

  def readQueries(dirName: String): Array[(String, Query)] = {
    val pattern = """q(.+)-.*.sparql""".r
    val dir = new File(dirName)
    if (dir == null || dir.listFiles == null)
      throw new IOException(s"Directory: ${dirName} not accessible")
    else {
      for (file <- dir.listFiles if file.getName endsWith ".sparql") yield {
        val queryName = file.getName match {
          case pattern(i) => i
          case _ => "UNKNOWN QUERY NAME"
        }
        (queryName, readQuery(file))
      }
    }
  }

  def readQuery(file: File): Query = {
    val contents = scala.io.Source.fromFile(file).mkString;
    QueryFactory.create(contents)
  }

  def executeQuery(model: Model, query: Query): Model = {
    val resultModel = ModelFactory.createDefaultModel
    val qexec = QueryExecutionFactory.create(query, model)
    qexec.execConstruct(resultModel)
    resultModel
  }

  def loadModel(model: Model, inputStream: InputStream, format: String = TURTLE) = {
    println("LOAD MODEL")
    model.read(inputStream, "", format)
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