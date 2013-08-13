package es.weso.utils

import com.hp.hpl.jena.rdf.model.LiteralRequiredException
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ResourceRequiredException
import com.hp.hpl.jena.rdf.model.Statement

object JenaUtils {

  val RdfXML = "RDF/XML"
  val RdfXMLAbbr = "RDF/XML-ABBREV"
  val NTriple = "N-TRIPLE"
  val Turtle = "TURTLE"
  val TTL = "TTL"
  val N3 = "N3"

  def extractModel(resource: Resource, model: Model): Model = {
    val nModel = ModelFactory.createDefaultModel()

    def inner(resource: Resource): Model = {
      val iterator2 = model.listStatements(resource, null, null)

      while (iterator2.hasNext()) {
        val stmt = iterator2.nextStatement();
        val subject = stmt.getSubject();
        val predicate = stmt.getPredicate();
        val objec = stmt.getObject();
        nModel.add(subject, predicate, objec)
        if (objec.isAnon) {
          inner(objec.asResource())
        }
      }
      nModel
    }
    inner(resource)
  }

  def statementAsString(statement: Statement): String = {
    val resource = try {
      statement.getResource().toString()
    } catch {
      case e: ResourceRequiredException => null
    }
    if (resource == null) {
      try {
        statement.getLiteral().toString()
      } catch {
        case e: LiteralRequiredException => resource
      }
    } else resource
  }
}