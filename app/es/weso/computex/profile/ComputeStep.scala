package es.weso.computex.profile

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
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.io.OutputStream
import scala.util.Random
import java.io.StringWriter
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.rdf.model.Literal
import java.io.FileOutputStream
import com.hp.hpl.jena.update.UpdateRequest
import com.hp.hpl.jena.update.UpdateFactory
import java.net.URI
import org.slf4j.LoggerFactory

/**
 * A computer step is made from a CONSTRUCT query
 * and a name. The construct query is supposed to generate
 * new triples that are added to a given model.
 * 
 */
case class ComputeStep(
    val query: 	Query,
    val name: 	String 		= "",
    val uri: 	URI 		= new URI("")
) {

  val logger = LoggerFactory.getLogger("Application")
  
  /**
   * Applies a construct query to a model 
   */
  def step(model:Model) : Model = {
    val resultModel = ModelFactory.createDefaultModel
    val qexec = QueryExecutionFactory.create(query, model)
    qexec.execConstruct(resultModel)
    logger.info("[step] ResultModel, size: " + resultModel.size)
    resultModel
  }
  
  /**
   * Computes a compute step on a model
   * @return a pair with the new model (the constructed triples added to the model) and the new triples
   */
  def compute(model:Model): (Model,Model) = {
    val newModel = step(model) 
    (model.add(newModel),newModel)
  }
  
}

object ComputeStep {

}
    
    

