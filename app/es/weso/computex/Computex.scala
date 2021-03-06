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
import es.weso.computex.entities.CIntegrityQuery
import es.weso.utils.JenaUtils.Turtle
import play.api.Logger
import es.weso.computex.entities.CQuery

/**
 * This class has been refactored as is not needed 
 * anymore
 */
case class Computex(
    val ontologyURI: String, 
    val validationDir: String,
    val computationDir: String, 
    val closureFile: String, 
    val flattenFile: String,
    val findStepsQuery: String) {

  def computex(message: CMessage): Array[CIntegrityQuery] = {
/*    Logger.info("Computex: Compute and Validate index data")
    val model = loadData(ontologyURI, message)
    val expandedCube = expandCube(model)
    val expandedComputex = if (message.expand) {
      expandComputex(model)
    } else { model }
    validate(expandedComputex, validationDir) */
    ???
  }

    def loadData(ontologyURI: String): Model = {
		val model = ModelFactory.createDefaultModel
		loadModel(model, Computex.loadFile(ontologyURI))
    }

  def loadData(ontologyURI: String, 
              indexDataURI: String) : Model = {
	val model = ModelFactory.createDefaultModel
	loadModel(model, Computex.loadFile(ontologyURI))
    loadModel(model, Computex.loadFile(indexDataURI))
  }
  
  def loadData(
      ontologyURI: String,
      message: CMessage
      ): Model = {
    /*
    val model = ModelFactory.createDefaultModel

    loadModel(model, Computex.loadFile(ontologyURI))
    loadModel(model, message.contentIS, message.contentFormat) */
    ???
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

  // This is only needed to obtain the list of expanders...can be removed
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

  // helper for getSteps
  private def collect(varName: String, result: ResultSet): List[String] = {
    val ls = result.toList
    ls.map(r => r.getLiteral(varName).getString)
  }

  // Old style validation
  def validate2Model(model: Model, validationDir: String): Model = {
    val reportModel = ModelFactory.createDefaultModel 
    val dir = new File(validationDir)
    for {
      vDir <- dir.listFiles.filter(_.isDirectory())
      cq <- readQueries(vDir)
    } {
      reportModel.add(executeConstructQuery(model, cq.query))
    }
    reportModel
  }

  // For each validator generates an CIntegrityQuery
  // Replace this validation Reports
/*
  def validate(model: Model, validationDir: String): Array[CIntegrityQuery] = {
    val dir = new File(validationDir)
    val iQueries: Array[CIntegrityQuery] = 
    for {
      vDir <- dir.listFiles.filter(_.isDirectory())
      cq <- readQueries(vDir)
      currentModel = executeConstructQuery(model, cq.query)
    } yield {
      currentModel.setNsPrefixes(model)
      Parser.parse(cq, currentModel)
    }

    val model = loadData(ontologyURI, message)
    val expandedCube = expandCube(model)
    val expandedComputex = if (message.expand) {
      expandComputex(model)
    } else { model }

    validate(expandedComputex, validationDir)
  }
*/
  // Should be replaced by Profiles
  def readQueries(dir: File): Array[CQuery] = {
    val pattern = """q(.+)-.*.sparql""".r
    if (dir == null || dir.listFiles == null)
      throw new IOException(s"Directory: ${dir.getName} not accessible")
    else {
      for (file <- dir.listFiles if file.getName endsWith ".sparql") yield {
        val queryName = file.getName match {
          case pattern(i) => i
          case _ => "UNKNOWN QUERY NAME"
        }
        CQuery(queryName, readQuery(file))
      }
    }
  }

  // maybe not necessary
  def readQuery(file: File): Query = {
    val contents = scala.io.Source.fromFile(file).mkString;
    QueryFactory.create(contents)
  }

  // helper model...to JenaUtils
  def executeConstructQuery(model: Model, query: Query): Model = {
    val resultModel = ModelFactory.createDefaultModel
    val qexec = QueryExecutionFactory.create(query, model)
    qexec.execConstruct(resultModel)
    resultModel
  }

  def loadTurtle(model: Model, fileName: String) = {
   model.read(fileName, "",Turtle)
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