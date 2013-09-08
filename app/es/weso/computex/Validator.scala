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
import es.weso.utils.JenaUtils._
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
import java.net.URI

/**
 * Contains a validation query. 
 * Validation queries have a name and a SPARQL CONSTRUCT query which constructs
 * an error model in case the model does not pass.
 * 
 */
case class Validator(
    val query: 	Query,
    val name: 	String = "",
    val uri: 	URI = new URI("")) {
  

  /**
   *  Validates a model using this validation 
   */
  def validate(model:Model) : ValidationReport[Model] = {
   val resultModel = ModelFactory.createDefaultModel
    val qexec = QueryExecutionFactory.create(query, model)
    qexec.execConstruct(resultModel)
    if (resultModel.size == 0) 
      Passed
    else 
      NotPassed(resultModel)
  }
  

  override def toString : String = {
    "Validator" + name + ". URI(" + uri + ") \n" +
    "Query: " + query    
  }

}

object Validator {

  /**
   * Create validators from queries represented as String
   * @param queryStr Validation SPARQL query as a String
   */
  def apply(queryStr: String) = 
    new Validator(parseQuery(queryStr).get)

}
    
    

