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

/**
 * Expands a Model with an UPDATE Query
 * 
 */
case class Expander(
    val update: UpdateRequest,
    val name: String = "",
    val uri: String = ""
) {
  
  /**
   * Expands a model 
   */
  def expand(model:Model) : Option[Model] = {
    val ds: Dataset = DatasetFactory.create(model)
    val graphStore: GraphStore = GraphStoreFactory.create(ds)
    UpdateAction.execute(update, graphStore)
    val result: Model = 
      ModelFactory.createModelForGraph(graphStore.getDefaultGraph())
    if (result.size > 0) Some(result)
      else None
  }
  
}

object Expander {

}
    
    

