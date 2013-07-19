package es.weso.computex

import scala.collection.mutable.ListBuffer
import es.weso.computex.entities.CErrorMessage
import es.weso.utils.JenaUtils
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.query.Query
import scala.collection.JavaConversions._
import es.weso.computex.entities._
import es.weso.utils.JenaUtils._
import java.io.ByteArrayOutputStream
import com.hp.hpl.jena.rdf.model.RDFNode

object Parser {

  def parse(query: (String, Query), model: Model): IntegrityQuery = {
    val iQuery = IntegrityQuery(query)
    iQuery.message = extractMessage(query._2)
    iQuery.errorMessages = parseErrors(model)
    iQuery
  }

  private def extractMessage(query: Query) = {
    val constructTemplate = query.getConstructTemplate()
    val lastTriple = constructTemplate.getTriples().toList.last.asTriple()
    val literal = lastTriple.getMatchObject().getLiteral()
    literal.getValue().toString()
  }

  private def parseErrors(model: Model): List[CErrorMessage] = {
    val computexError: Property = model.getProperty("http://purl.org/weso/ontology/computex#Error")
    val computexMsg: Property = model.getProperty("http://purl.org/weso/ontology/computex#msg")

    val iterator = model.listStatements(null, null, computexError)

    val errors: ListBuffer[CErrorMessage] = ListBuffer.empty

    while (iterator.hasNext()) {
      val stmt = iterator.nextStatement();
      val subject = stmt.getSubject();

      val subModel = JenaUtils.extractModel(subject, model)

      val innerIteratorMsg = subModel.listObjectsOfProperty(computexMsg)

      val msg = if (innerIteratorMsg.hasNext()) {
        innerIteratorMsg.next().asLiteral().toString()
      } else ""
        
      val params = extractParams(subModel)

      errors += CErrorMessage(msg, params, CModel(subModel))

    }
    errors.toList
  }
  
  private def extractParams(model: Model) : Array[CParam] = {
    val computexParam: Property = model.getProperty("http://purl.org/weso/ontology/computex#errorParam")
    val computexName = model.getProperty("http://purl.org/weso/ontology/computex#name")
    val computexValue = model.getProperty("http://purl.org/weso/ontology/computex#value")
    val seq : Seq[RDFNode] = model.listObjectsOfProperty(computexParam).toList
    model.write(System.out)
    for {
      next <- seq.toArray
      node = next.asResource()
      foo = model.listStatements(node, computexName, null).nextStatement()
      name = model.listStatements(node, computexName, null).nextStatement().getLiteral().getString()
      value = model.listStatements(node, computexValue, null).nextStatement().getResource().toString()
    } yield {
      CParam(name, value)
    }
  }
}