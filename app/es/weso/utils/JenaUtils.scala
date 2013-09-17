package es.weso.utils

import com.hp.hpl.jena.rdf.model.LiteralRequiredException
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ResourceRequiredException
import com.hp.hpl.jena.rdf.model.Statement
import java.io.InputStreamReader
import java.io.ByteArrayInputStream
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import java.io.StringWriter
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.Property
import java.net.URI
import java.net.URL
import java.io.InputStream
import java.io.FileOutputStream
import org.apache.jena.atlas.AtlasException
import org.apache.jena.riot.RiotException

sealed abstract class ParserReport[+A,+B] 

final case class Parsed[A](info:A) 
	extends ParserReport[A,Nothing]

final case class NotParsed[B](error: B) 
	extends ParserReport[Nothing,B]

object JenaUtils {

  val RdfXML 		= "RDF/XML"
  val RdfXMLAbbr 	= "RDF/XML-ABBREV"
  val NTriple 		= "N-TRIPLE"
  val Turtle 		= "TURTLE"
  val TTL 			= "TTL"
  val N3 			= "N3"

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

  def dereferenceURI( uri: String ) : InputStream = {
    val url = new URL(uri)
    val urlCon = url.openConnection()
    urlCon.setConnectTimeout(4000)
    urlCon.setReadTimeout(2000)
    urlCon.getInputStream()
  }

  def parseFromURI(uri: String, 
      base: String = "", 
      syntax: String = Turtle) : Model = {
    uri2Model(uri,base,syntax) match {
      case Parsed(model) => model
      case NotParsed(err) => throw new Exception("Cannot parse from URI: " + uri + ". Error: " + err)
    }
  }

  def parseFromString(
      content: String, 
      base: String = "", 
      syntax: String = Turtle) : Model = {
    str2Model(content,base,syntax) match {
      case Parsed(model) => model
      case NotParsed(err) => 
        throw new Exception("Cannot parse from string: " + content + ". Error: " + err + ". Syntax: " + syntax)
    }
  }

  def uri2Model(
      uriName: String,
      base: String = "",
      syntax: String = Turtle) : ParserReport[Model,String] = {
    try {
      val model = ModelFactory.createDefaultModel()
      Parsed(model.read(dereferenceURI(uriName),base,syntax))
    } catch {
      case e@(_: AtlasException | _: RiotException) => 
        NotParsed("Bad formed with syntax " + syntax + ". " + e.getLocalizedMessage())
      case e : Exception =>
        NotParsed("Exception parsing from URI " + uriName + 
            " with syntax " + syntax + ". " + e.getLocalizedMessage())
    }
  }
  

  /**
   * Returns a RDF model after parsing a String
   */
  def str2Model(
      str: String,
	  base: String = "",
	  syntax: String = Turtle) : ParserReport[Model,String] = {
    try {
      val model = ModelFactory.createDefaultModel()
      val stream = new ByteArrayInputStream(str.getBytes("UTF-8"))
      Parsed(model.read(stream,base,syntax))
    } catch {
      case e@(_: AtlasException | _: RiotException) => 
        NotParsed("Bad formed with syntax " + syntax + ". " + e.getLocalizedMessage())
      case e : Exception =>
        NotParsed("Exception parsing from String " + str + 
            " with syntax " + syntax + ". " + e.getLocalizedMessage())
    }
  } 
    
  /**
   * Returns a RDF model after parsing an InputStream
   */
  def parseInputStream(
      stream: 	InputStream,
	  base: 	String = "",
	  syntax: 	String = Turtle) : ParserReport[Model,String] = {
    try {
      val model = ModelFactory.createDefaultModel()
      Parsed(model.read(stream,base,syntax))
    } catch {
      case e@(_: AtlasException | _: RiotException) => 
        NotParsed("Bad formed with syntax " + syntax + ". " + e.getLocalizedMessage())
      case e : Exception =>
        NotParsed("Exception parsing " + 
            " with syntax " + syntax + ". " + e.getLocalizedMessage())
    }
  } 

  def getLiteral(
       r : RDFNode, 
       property: Property) : String = {
    if (r.isResource()) {
        val res = r.asResource
    	val stmt = res.getRequiredProperty(property)
    	stmt match {
    	  case null => 
    	    throw new Exception("getName: " + res + " doesn't have value for property " + property + ".\n" + 
    	    					showResource(r.asResource) )
    	  case _ => 
    	    if (stmt.getObject.isLiteral) stmt.getObject.asLiteral.getString
    	    else 
    	      throw new Exception("getName: " + stmt.getObject + " is not a literal")
    	}
    } 
    else 
      throw new Exception("getName: " + r + "is not a resource")
  }

  
  /*
   * 
   */
  def getURI(r: RDFNode) : URI = {
    if (r.isResource) {
      new URI(r.asResource.getURI)
    }
    else 
       throw new Exception("getURI: Node " + r + " is not a resource")
  }
   
  /*
   * If there is a triple <r,p,u> and u is a URI, returns u
   * @param r RDFNode
   * @param property
   */
  def getObjectURI(
		  r: RDFNode, 
		  p: Property
	) : URI = {
    if (r.isResource()) {
    	val resUri = r.asResource().getPropertyResourceValue(p)
    	resUri match {
    	  case null => 
    	    throw new Exception("getURI: " + resUri + " doesn't have value for property " + p + ".\n" + showResource(r.asResource) )
    	  case _ => 
    	    getURI(resUri)
        }
     } else 
       throw new Exception("getURI: Node " + r + " is not a resource")
  }

  /**
   * Shows infomation about a resource (list all the statements)
   */
  def showResource(resource: Resource) : String = {
   val sb = new StringBuilder
   val iter = resource.listProperties()
   sb ++= ("Infor about: " + resource + "\n")
   while (iter.hasNext) {
     val st = iter.next
     sb ++= (st.toString + "\n")
   }
   sb.toString
  }

  /*
   * Parse a string to obtain a query
   */
  def parseQuery(
      str: String
      ) : Option[Query] = {
   try {
     val query = QueryFactory.create(str)
     Some(query)
   } catch {
   	 case e: Exception => None
   }
  }
  
 
  /*
   * Convert a model to a String
   */
  def model2Str(
		  model: Model, 
		  syntax: String = Turtle) : String = {
    val strWriter = new StringWriter
    model.write(strWriter,syntax)
    strWriter.toString
  }

  /*
   * Write a model to a file
   */
  def model2File(
		  model: Model, 
		  fileName : String,
		  syntax: String = Turtle) : Unit = {
    model.write(new FileOutputStream(fileName),syntax)
  }

}