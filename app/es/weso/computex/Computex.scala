package es.weso.computex

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asScalaIterator
import scala.io.Source.fromFile
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.DatasetFactory
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.update.GraphStore
import com.hp.hpl.jena.update.GraphStoreFactory
import com.hp.hpl.jena.update.UpdateAction
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.IntegrityQuery
import es.weso.utils.JenaUtils.Turtle
import play.api.Logger

case class Computex(val ontologyURI: String, val validationDir: String,
  val computationDir: String, val closureFile: String, val flattenFile: String,
  val findStepsQuery: String) {

  def computex(message: CMessage): Array[(String, IntegrityQuery)] = {
    Logger.info("Computex: Compute and Validate index data")
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

    for {
      step <- steps
      pattern = s"(.*q.*-${step}.sparql)".r
      file <- new File(computationDir).listFiles
    } {
      file.getAbsolutePath() match {
        case pattern(name) =>
          val contents = fromFile(name).mkString
          UpdateAction.parseExecute(contents, graphStore)
        case _ => {}
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
      currentModel.setNsPrefixes(model)
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

  def loadModel(model: Model, inputStream: InputStream, format: String = Turtle) = {
    model.read(inputStream, "", format)
  }

}

object Computex {

  def loadFile(fileName: String): InputStream = {
    val url = new URL(fileName)
    val urlCon = url.openConnection()
    urlCon.setConnectTimeout(4000)
    urlCon.setReadTimeout(2000)
    urlCon.getInputStream()
  }
}