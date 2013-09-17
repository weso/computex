package es.weso.computex

import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.RDFNode
import es.weso.computex.entities.ErrorMessage
import es.weso.computex.entities.CModel
import es.weso.computex.entities.CParam
import es.weso.computex.entities.CIntegrityQuery
import es.weso.utils.JenaUtils
import es.weso.utils.JenaUtils.statementAsString
import es.weso.computex.entities.CQuery

object Parser {

  def parse(query: CQuery, model: Model): CIntegrityQuery = {
    CIntegrityQuery(query, 
    				extractMessage(query.query), 
    				parseErrors(model)
    				)
  }

  private def extractMessage(query: Query) = {
    val constructTemplate = query.getConstructTemplate()
    val lastTriple = constructTemplate.getTriples().toList.last.asTriple()
    val literal = lastTriple.getMatchObject().getLiteral()
    literal.getValue().toString()
  }

  private def parseErrors(model: Model): List[ErrorMessage] = {
    val computexError: Property = model.getProperty("http://purl.org/weso/ontology/computex#Error")
    val computexMsg: Property = model.getProperty("http://purl.org/weso/ontology/computex#msg")

    val iterator = model.listStatements(null, null, computexError)

    val errors: ListBuffer[ErrorMessage] = ListBuffer.empty

    while (iterator.hasNext()) {
      val stmt = iterator.nextStatement();
      val subject = stmt.getSubject();

      val subModel = JenaUtils.extractModel(subject, model)

      val innerIteratorMsg = subModel.listObjectsOfProperty(computexMsg)

      val msg = if (innerIteratorMsg.hasNext()) {
        innerIteratorMsg.next().asLiteral().toString()
      } else ""
        
      val params = extractParams(subModel)

      errors += ErrorMessage(params, new CModel(subModel))

    }
    errors.toList
  }
  
  private def extractParams(model: Model) : Array[CParam] = {
    val computexParam: Property = model.getProperty("http://purl.org/weso/ontology/computex#errorParam")
    val computexName = model.getProperty("http://purl.org/weso/ontology/computex#name")
    val computexValue = model.getProperty("http://purl.org/weso/ontology/computex#value")
    val seq : Seq[RDFNode] = model.listObjectsOfProperty(computexParam).toList
    for {
      next <- seq.toArray
      node = next.asResource()
      foo = model.listStatements(node, computexName, null).nextStatement()
      name = statementAsString(model.listStatements(node, computexName, null).nextStatement())
      value = statementAsString(model.listStatements(node, computexValue, null).nextStatement())
    } yield {
      CParam(name, value)
    }
  }
}