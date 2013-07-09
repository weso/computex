package es.weso.computex

import scala.collection.mutable.ListBuffer
import es.weso.computex.entities.ErrorMessage
import es.weso.utils.JenaUtils
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.query.Query
import scala.collection.JavaConversions._
import es.weso.computex.entities._
import es.weso.utils.JenaUtils._
import java.io.ByteArrayOutputStream

object Parser {
  
  def parse (query:(String,Query), model:Model) : IntegrityQuery = {
    val iQuery = IntegrityQuery(query)
    iQuery.message = extractMessage(query._2)
    iQuery.errorMessages = parseErrors(model)
    iQuery
  }
  
  private def extractMessage(query:Query) = {
    val constructTemplate = query.getConstructTemplate()
    val lastTriple = constructTemplate.getTriples().toList.last.asTriple()
    val literal = lastTriple.getMatchObject().getLiteral()
    literal.getValue().toString()
  }
  
  private def parseErrors(model: Model) : List[ErrorMessage] = {
    val computexError: Property = model.getProperty("http://purl.org/weso/ontology/computex#Error")
    val computexMsg: Property = model.getProperty("http://purl.org/weso/ontology/computex#msg")
    val iterator = model.listStatements(null, null, computexError)

    val errors: ListBuffer[ErrorMessage] = ListBuffer.empty

    while (iterator.hasNext()) {
      val stmt = iterator.nextStatement();
      val subject = stmt.getSubject();

      val subModel = JenaUtils.extractModel(subject, model)

      val innerIterator = subModel.listObjectsOfProperty(computexMsg)

      val msg = if (innerIterator.hasNext()) {
        innerIterator.next().asLiteral().toString()
      } else ""
        
      errors += ErrorMessage(msg, CModel(subModel))

    }
    errors.toList
  }
}