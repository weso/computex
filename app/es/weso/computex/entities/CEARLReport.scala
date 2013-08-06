package es.weso.computex.entities

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import es.weso.utils.DateUtils
import scala.io.Source
import java.io.FileOutputStream
import java.io.File

case class CEARLReport(message : CMessage) {
  
  private val model : Model = ModelFactory.createDefaultModel()
  
  initializeModel
  addAssertor
  addTested
  addResults
  
  def saveModel() : Unit = {
    model.write(new FileOutputStream(new File("earlreport.ttl")), "TURTLE")
  }
  
  private def addResults() = {
    var assertionId : Int = 1
    var errorId : Int = 1
    message.integrityQueries.foreach(tuple => {
      val iq : IntegrityQuery = tuple._2
      if(iq.errorMessages.size > 0) {
        createFailedTest(assertionId, errorId, iq)
        errorId += 1
      } else {
        createPassedTest(assertionId, iq)
      }
      assertionId += 1
    })
  }
  
  private def createFailedTest(assertionId : Int, errorId : Int, 
      iq : IntegrityQuery) = {
    iq.errorMessages.foreach(err => {
      val error : Resource = generateErrorResource("error" + errorId, "failed",
          err.message)
      generateAssertionResource(error, assertionId, iq.message)      
    })
  }
  
  private def generateErrorResource(id : String, result : String, 
      message : String) : Resource = {
    val error : Resource = model.createResource(CEARLReport.PREFIX_CEXREPORT + 
        id)
    error.addProperty(CEARLReport.PROPERTY_RDF_ID, id)
    error.addProperty(CEARLReport.PROPERTY_RDF_TYPE, 
        ResourceFactory.createResource(CEARLReport.PREFIX_EARL + "TestResult"))
     error.addProperty(CEARLReport.PROPERTY_EARL_OUTCOME, 
    	ResourceFactory.createResource(CEARLReport.PREFIX_EARL + result))
	error.addProperty(CEARLReport.PROPERTY_DC_DESCRIPTION, 
	    ResourceFactory.createLangLiteral(message, "en"))
    error
  }
  
  private def generateAssertionResource(errorResource : Resource, id : Int, 
      message : String)	: Resource = {
    val assertion : Resource = model.createResource(
	    CEARLReport.PREFIX_CEXREPORT + "ass" + id)
	assertion.addProperty(CEARLReport.PROPERTY_RDF_ID, "ass" + id)
    assertion.addProperty(CEARLReport.PROPERTY_RDF_TYPE, 
        ResourceFactory.createResource(CEARLReport.PREFIX_EARL + "Assertion"))
    assertion.addProperty(CEARLReport.PROPERTY_EARL_RESULT, errorResource)
    assertion.addProperty(CEARLReport.PROPERTY_EARL_SUBJECT, 
        ResourceFactory.createResource("http://purl.org/weso/ontology/computex/earl-report#fileContent"))
    assertion.addProperty(CEARLReport.PROPERTY_EARL_ASSERTEDBY, 
        ResourceFactory.createResource(CEARLReport.PREFIX_CEXREPORT + "WESO"))
    assertion.addProperty(CEARLReport.PROPERTY_EARL_TEST, addCriterion)
    assertion.addProperty(CEARLReport.PROPERTY_DC_DESCRIPTION, 
	    ResourceFactory.createTypedLiteral(message, XSDDatatype.XSDstring))
    assertion
  }
  
  private def createPassedTest(id : Int, iq : IntegrityQuery) = {
    val error : Resource = generateErrorResource("passed", "passed", 
        "Passed test")
     generateAssertionResource(error, id, iq.message)
  }
  
  private def addCriterion() : Resource = {
    val criterion : Resource = model.createResource("http://www.w3.org/TR/2013/CR-vocab-data-cube-20130625/#wf")
    criterion.addProperty(CEARLReport.PROPERTY_RDF_TYPE, 
        ResourceFactory.createResource(CEARLReport.PREFIX_EARL + 
            "TestRequirement"))
    criterion.addProperty(CEARLReport.PROPERTY_DC_TITLE, 
        ResourceFactory.createLangLiteral(
            "RDF Data Cube Integrity constraints definition", "en"))
    criterion.addProperty(CEARLReport.PROPERTY_DC_DESCRIPTION, 
        ResourceFactory.createLangLiteral(
            "A set of integrity constraints that an instance of an RDF Data " +
            "Cube should be conformed", "en"))
    criterion
  }
  
  private def addTested() = {
    //We have to give an URI to identify the data that we tested
    val tested : Resource = model.createResource(CEARLReport.PREFIX_CEXREPORT + 
        "fileContent")
    tested.addProperty(CEARLReport.PROPERTY_RDF_TYPE,
        ResourceFactory.createResource(CEARLReport.PREFIX_CNT + 
            "ContentAsText"))
    tested.addProperty(CEARLReport.PROPERTY_DC_TITLE, 
        ResourceFactory.createLangLiteral("The file with RDF Data Cube and " +
    		"Statistical indexes with computations that have to be tested", "en"))
	tested.addProperty(CEARLReport.PROPERTY_DC_DATE, 
	    ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, 
	        XSDDatatype.XSDstring))
    tested.addProperty(CEARLReport.PROPERTY_CNT_CHARACTERENCODING, 
        ResourceFactory.createTypedLiteral("UTF-8", XSDDatatype.XSDstring))
    tested.addProperty(CEARLReport.PROPERTY_CNT_CHARS, 
        ResourceFactory.createTypedLiteral(
            Source.fromInputStream(message.contentIS).mkString , 
            XSDDatatype.XSDstring))
    tested
  }
  
  private def addAssertor() : Resource = {
    val assertor : Resource = 
      model.createResource("http://computex.herokuapp.com")
    assertor.addProperty(CEARLReport.PROPERTY_DC_TITLE, 
        ResourceFactory.createLangLiteral("RDF Data Cube Validator", "en"))
    assertor.addProperty(CEARLReport.PROPERTY_DC_DESCRIPTION, 
        ResourceFactory.createLangLiteral("RDF Data Cube Validator Service," +
        		" a free service that validates index data files and " +
        		"performs computations on them", "en"))
	assertor.addProperty(CEARLReport.PROPERTY_RDF_TYPE, 
	    ResourceFactory.createResource(CEARLReport.PREFIX_EARL + "Software"))
    assertor.addProperty(CEARLReport.PROPERTY_DC_HASVERSION, 
        ResourceFactory.createTypedLiteral("0.0.1", XSDDatatype.XSDstring))
    
    val group : Resource = model.createResource(CEARLReport.PREFIX_CEXREPORT 
        + "WESO")
    group.addProperty(CEARLReport.PROPERTY_RDF_TYPE, 
        ResourceFactory.createResource(CEARLReport.PREFIX_FOAF + "Group"))
    group.addProperty(CEARLReport.PROPERTY_EARL_MAINASSERTOR, assertor)
    group.addProperty(CEARLReport.PROPERTY_DC_TITLE, 
        ResourceFactory.createTypedLiteral("WESO & RDF Data Cube Validator", 
            XSDDatatype.XSDstring))
    group.addProperty(CEARLReport.PROPERTY_FOAF_MEMBER, 
        createMember("mailto:jelabra@gmail.com", "Jose Emilio Labra Gayo"))
    group.addProperty(CEARLReport.PROPERTY_FOAF_MEMBER, 
        createMember("mailto:chema.ar@gmail.com", "Jose Maria Alvarez " +
        		"Rodriguez"))
	group.addProperty(CEARLReport.PROPERTY_FOAF_MEMBER, 
        createMember("mailto:ignacio.fuertes@weso.es", 
            "Ignacio Fuertes Bernardo"))
    group.addProperty(CEARLReport.PROPERTY_FOAF_MEMBER, 
        createMember("mailto:cesar.luis@weso.es", "Cesar Luis Alvargonzalez"))
    group.addProperty(CEARLReport.PROPERTY_FOAF_MEMBER, 
        createMember("mailto:alejandro.montes@gmail.com", 
            "Alejandro Montes Garcia"))
    group.addProperty(CEARLReport.PROPERTY_FOAF_MEMBER, 
        createMember("mailto:castrofernandez@gmail.com", 
            "Juan Castro Fernandez"))
    group
  }
  
  private def createMember(email : String, name : String) : Resource = {
    val member : Resource = model.createResource
    member.addProperty(CEARLReport.PROPERTY_RDF_TYPE, 
        ResourceFactory.createResource(CEARLReport.PREFIX_FOAF + "Person"))
    member.addProperty(CEARLReport.PROPERTY_FOAF_MBOX, 
		ResourceFactory.createResource(email))
	member.addProperty(CEARLReport.PROPERTY_FOAF_NAME, 
	    ResourceFactory.createTypedLiteral(name, XSDDatatype.XSDstring))
    member
  }
  
  private def initializeModel() = {
    model.setNsPrefix("rdf", CEARLReport.PREFIX_RDF)
    model.setNsPrefix("earl", CEARLReport.PREFIX_EARL)
    model.setNsPrefix("rdfs", CEARLReport.PREFIX_RDFS)
    model.setNsPrefix("cnt", CEARLReport.PREFIX_CNT)
    model.setNsPrefix("dc", CEARLReport.PREFIX_DC)
    model.setNsPrefix("doap", CEARLReport.PREFIX_DOAP)
    model.setNsPrefix("foaf", CEARLReport.PREFIX_FOAF)
    model.setNsPrefix("ptr", CEARLReport.PREFIX_PTR)
    model.setNsPrefix("xsd", CEARLReport.PREFIX_XSD)
    model.setNsPrefix("cex-earl", CEARLReport.PREFIX_CEXREPORT)
  }

}

object CEARLReport {
  val PREFIX_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val PREFIX_EARL = "http://www.w3.org/ns/earl#"
  val PREFIX_RDFS = "http://www.w3.org/2000/01/rdf-schema#"
  val PREFIX_CNT = "http://www.w3.org/2011/content#"
  val PREFIX_DC = "http://purl.org/dc/terms/"
  val PREFIX_DOAP = "http://usefulinc.com/ns/doap#"
  val PREFIX_FOAF = "http://xmlns.com/foaf/0.1/#"
  val PREFIX_PTR = "http://www.w3.org/2009/pointers#"
  val PREFIX_XSD = "http://www.w3.org/2001/XMLSchema#"
  val PREFIX_CEXREPORT = "http://purl.org/weso/ontology/computex/earl-report#"  
    
    
  val PROPERTY_DC_TITLE = ResourceFactory.createProperty(PREFIX_DC + "title")
  val PROPERTY_DC_DESCRIPTION = ResourceFactory.createProperty(PREFIX_DC + 
      "description")
  val PROPERTY_RDF_TYPE = ResourceFactory.createProperty(PREFIX_RDF + "type")
  val PROPERTY_DC_HASVERSION = ResourceFactory.createProperty(PREFIX_DC + 
      "hasVersion")
  val PROPERTY_EARL_MAINASSERTOR = ResourceFactory.createProperty(PREFIX_EARL +
      "mainAssertor")
  val PROPERTY_FOAF_MEMBER = ResourceFactory.createProperty(PREFIX_FOAF + 
      "member")
  val PROPERTY_FOAF_MBOX = ResourceFactory.createProperty(PREFIX_FOAF + "mbox")
  val PROPERTY_FOAF_NAME = ResourceFactory.createProperty(PREFIX_FOAF + "name")
  val PROPERTY_DC_DATE = ResourceFactory.createProperty(PREFIX_DC + "date")
  val PROPERTY_CNT_CHARACTERENCODING = ResourceFactory.createProperty(PREFIX_CNT
      + "characterEncoding")
  val PROPERTY_CNT_CHARS = ResourceFactory.createProperty(PREFIX_CNT + "chars")
  val PROPERTY_RDF_ID = ResourceFactory.createProperty(PREFIX_RDF + "ID")
  val PROPERTY_EARL_OUTCOME = ResourceFactory.createProperty(PREFIX_EARL + 
      "outcome")
  val PROPERTY_EARL_RESULT = ResourceFactory.createProperty(PREFIX_EARL + 
      "result")
  val PROPERTY_EARL_SUBJECT = ResourceFactory.createProperty(PREFIX_EARL + 
      "subject")
  val PROPERTY_EARL_ASSERTEDBY = ResourceFactory.createProperty(PREFIX_EARL + 
      "assertedBy")
  val PROPERTY_EARL_TEST = ResourceFactory.createProperty(PREFIX_EARL + "test")
  
  
  def main(args: Array[String]): Unit = {
//    val report = CEARLReport("mensaje")
//    report.printModel
  }
}