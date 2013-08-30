package es.weso.computex.entities

import java.io.File
import java.io.FileOutputStream
import scala.io.Source
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ResourceFactory
import es.weso.utils.DateUtils
import java.util.Date
import java.util.UUID

case class CEARLReport(message: CMessage) {

  private val model: Model = ModelFactory.createDefaultModel()

  initializeModel
  addAssertor
  addTested
  addResults

  def saveModel(): String = {
    val hash = UUID.randomUUID().toString()
    val timestamp = new Date().getTime()
    val fileName = s"earlreport-${hash}-${timestamp}.ttl"
    model.write(new FileOutputStream(new File(s"public/earls/${fileName}")), "TURTLE")
    s"earl/${fileName}"
  }

  private def addResults() = {
    var assertionId: Int = 1
    var errorId: Int = 1
    message.integrityQueries.foreach(iq => {
      if (iq.errorMessages.size > 0) {
        createFailedTest(assertionId, errorId, iq)
        errorId += 1
      } else {
        createPassedTest(assertionId, iq)
      }
      assertionId += 1
    })
  }

  private def createFailedTest(assertionId: Int, errorId: Int,
    iq: CIntegrityQuery) = {
    iq.errorMessages.foreach(err => {
      val error: Resource = generateErrorResource("error" + errorId, "failed",
        iq.message)
      generateAssertionResource(error, assertionId, iq.message)
    })
  }

  private def generateErrorResource(id: String, result: String,
    message: String): Resource = {
    val error: Resource = model.createResource(CEARLReport.PrefixCEXREPORT +
      id)
    error.addProperty(CEARLReport.PropertyRDFId, id)
    error.addProperty(CEARLReport.PropertyRDFType,
      ResourceFactory.createResource(CEARLReport.PrefixEARL + "TestResult"))
    error.addProperty(CEARLReport.PropertyEARLOutcome,
      ResourceFactory.createResource(CEARLReport.PrefixEARL + result))
    error.addProperty(CEARLReport.PropertyDCDescription,
      ResourceFactory.createLangLiteral(message, "en"))
    error
  }

  private def generateAssertionResource(errorResource: Resource, id: Int,
    message: String): Resource = {
    val assertion: Resource = model.createResource(
      CEARLReport.PrefixCEXREPORT + "ass" + id)
    assertion.addProperty(CEARLReport.PropertyRDFId, "ass" + id)
    assertion.addProperty(CEARLReport.PropertyRDFType,
      ResourceFactory.createResource(CEARLReport.PrefixEARL + "Assertion"))
    assertion.addProperty(CEARLReport.PropertyEARLResult, errorResource)
    assertion.addProperty(CEARLReport.PropertyEARLSubject,
      ResourceFactory.createResource("http://purl.org/weso/ontology/computex/earl-report#fileContent"))
    assertion.addProperty(CEARLReport.PropertyEARLAssertebdy,
      ResourceFactory.createResource(CEARLReport.PrefixCEXREPORT + "WESO"))
    assertion.addProperty(CEARLReport.PropertyEARLTest, addCriterion)
    assertion.addProperty(CEARLReport.PropertyDCDescription,
      ResourceFactory.createTypedLiteral(message, XSDDatatype.XSDstring))
    assertion
  }

  private def createPassedTest(id: Int, iq: CIntegrityQuery) = {
    val error: Resource = generateErrorResource("passed", "passed",
      "Passed test")
    generateAssertionResource(error, id, iq.message)
  }

  private def addCriterion(): Resource = {
    val criterion: Resource = model.createResource("http://www.w3.org/TR/2013/CR-vocab-data-cube-20130625/#wf")
    criterion.addProperty(CEARLReport.PropertyRDFType,
      ResourceFactory.createResource(CEARLReport.PrefixEARL +
        "TestRequirement"))
    criterion.addProperty(CEARLReport.PropertyDCTitle,
      ResourceFactory.createLangLiteral(
        "RDF Data Cube Integrity constraints definition", "en"))
    criterion.addProperty(CEARLReport.PropertyDCDescription,
      ResourceFactory.createLangLiteral(
        "A set of integrity constraints that an instance of an RDF Data " +
          "Cube should be conformed", "en"))
    criterion
  }

  private def addTested() = {
    //We have to give an URI to identify the data that we tested
    val tested: Resource = model.createResource(CEARLReport.PrefixCEXREPORT +
      "fileContent")
    tested.addProperty(CEARLReport.PropertyRDFType,
      ResourceFactory.createResource(CEARLReport.PrefixCNT +
        "ContentAsText"))
    tested.addProperty(CEARLReport.PropertyDCTitle,
      ResourceFactory.createLangLiteral("The file with RDF Data Cube and " +
        "Statistical indexes with computations that have to be tested", "en"))
    tested.addProperty(CEARLReport.PropertyDCDate,
      ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString,
        XSDDatatype.XSDstring))
    tested.addProperty(CEARLReport.PropertyCNTCharacterEncoding,
      ResourceFactory.createTypedLiteral("UTF-8", XSDDatatype.XSDstring))
    tested.addProperty(CEARLReport.PropertyCNTChars,
      ResourceFactory.createTypedLiteral(
        if (message.contentIS == null) ""
        else Source.fromInputStream(message.contentIS).mkString,
        XSDDatatype.XSDstring))
    tested
  }

  private def addAssertor(): Resource = {
    val assertor: Resource =
      model.createResource("http://computex.herokuapp.com")
    assertor.addProperty(CEARLReport.PropertyDCTitle,
      ResourceFactory.createLangLiteral("RDF Data Cube Validator", "en"))
    assertor.addProperty(CEARLReport.PropertyDCDescription,
      ResourceFactory.createLangLiteral("RDF Data Cube Validator Service," +
        " a free service that validates index data files and " +
        "performs computations on them", "en"))
    assertor.addProperty(CEARLReport.PropertyRDFType,
      ResourceFactory.createResource(CEARLReport.PrefixEARL + "Software"))
    assertor.addProperty(CEARLReport.PropertyDCHasversion,
      ResourceFactory.createTypedLiteral("0.0.1", XSDDatatype.XSDstring))

    val group: Resource = model.createResource(CEARLReport.PrefixCEXREPORT
      + "WESO")
    group.addProperty(CEARLReport.PropertyRDFType,
      ResourceFactory.createResource(CEARLReport.PrefixFOAF + "Group"))
    group.addProperty(CEARLReport.PropertyEARLMAinassertor, assertor)
    group.addProperty(CEARLReport.PropertyDCTitle,
      ResourceFactory.createTypedLiteral("WESO & RDF Data Cube Validator",
        XSDDatatype.XSDstring))
    group.addProperty(CEARLReport.PropertyFOAFMember,
      createMember("mailto:jelabra@gmail.com", "Jose Emilio Labra Gayo"))
    group.addProperty(CEARLReport.PropertyFOAFMember,
      createMember("mailto:chema.ar@gmail.com", "Jose Maria Alvarez " +
        "Rodriguez"))
    group.addProperty(CEARLReport.PropertyFOAFMember,
      createMember("mailto:ignacio.fuertes@weso.es",
        "Ignacio Fuertes Bernardo"))
    group.addProperty(CEARLReport.PropertyFOAFMember,
      createMember("mailto:cesar.luis@weso.es", "Cesar Luis Alvargonzalez"))
    group.addProperty(CEARLReport.PropertyFOAFMember,
      createMember("mailto:alejandro.montes@gmail.com",
        "Alejandro Montes Garcia"))
    group.addProperty(CEARLReport.PropertyFOAFMember,
      createMember("mailto:castrofernandez@gmail.com",
        "Juan Castro Fernandez"))
    group
  }

  private def createMember(email: String, name: String): Resource = {
    val member: Resource = model.createResource
    member.addProperty(CEARLReport.PropertyRDFType,
      ResourceFactory.createResource(CEARLReport.PrefixFOAF + "Person"))
    member.addProperty(CEARLReport.PropertyFOAFMbox,
      ResourceFactory.createResource(email))
    member.addProperty(CEARLReport.PropertyFOAFName,
      ResourceFactory.createTypedLiteral(name, XSDDatatype.XSDstring))
    member
  }

  private def initializeModel() = {
    model.setNsPrefix("rdf", CEARLReport.PrefixRDF)
    model.setNsPrefix("earl", CEARLReport.PrefixEARL)
    model.setNsPrefix("rdfs", CEARLReport.PrefixRDFS)
    model.setNsPrefix("cnt", CEARLReport.PrefixCNT)
    model.setNsPrefix("dc", CEARLReport.PrefixDC)
    model.setNsPrefix("doap", CEARLReport.PrefixDOAP)
    model.setNsPrefix("foaf", CEARLReport.PrefixFOAF)
    model.setNsPrefix("ptr", CEARLReport.PrefixPTR)
    model.setNsPrefix("xsd", CEARLReport.PrefixXSD)
    model.setNsPrefix("cex-earl", CEARLReport.PrefixCEXREPORT)
  }

}

object CEARLReport {
  val PrefixRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val PrefixEARL = "http://www.w3.org/ns/earl#"
  val PrefixRDFS = "http://www.w3.org/2000/01/rdf-schema#"
  val PrefixCNT = "http://www.w3.org/2011/content#"
  val PrefixDC = "http://purl.org/dc/terms/"
  val PrefixDOAP = "http://usefulinc.com/ns/doap#"
  val PrefixFOAF = "http://xmlns.com/foaf/0.1/#"
  val PrefixPTR = "http://www.w3.org/2009/pointers#"
  val PrefixXSD = "http://www.w3.org/2001/XMLSchema#"
  val PrefixCEXREPORT = "http://purl.org/weso/ontology/computex/earl-report#"

  val PropertyDCTitle = ResourceFactory.createProperty(PrefixDC + "title")
  val PropertyDCDescription = ResourceFactory.createProperty(PrefixDC +
    "description")
  val PropertyRDFType = ResourceFactory.createProperty(PrefixRDF + "type")
  val PropertyDCHasversion = ResourceFactory.createProperty(PrefixDC +
    "hasVersion")
  val PropertyEARLMAinassertor = ResourceFactory.createProperty(PrefixEARL +
    "mainAssertor")
  val PropertyFOAFMember = ResourceFactory.createProperty(PrefixFOAF +
    "member")
  val PropertyFOAFMbox = ResourceFactory.createProperty(PrefixFOAF + "mbox")
  val PropertyFOAFName = ResourceFactory.createProperty(PrefixFOAF + "name")
  val PropertyDCDate = ResourceFactory.createProperty(PrefixDC + "date")
  val PropertyCNTCharacterEncoding = ResourceFactory.createProperty(PrefixCNT
    + "characterEncoding")
  val PropertyCNTChars = ResourceFactory.createProperty(PrefixCNT + "chars")
  val PropertyRDFId = ResourceFactory.createProperty(PrefixRDF + "ID")
  val PropertyEARLOutcome = ResourceFactory.createProperty(PrefixEARL +
    "outcome")
  val PropertyEARLResult = ResourceFactory.createProperty(PrefixEARL +
    "result")
  val PropertyEARLSubject = ResourceFactory.createProperty(PrefixEARL +
    "subject")
  val PropertyEARLAssertebdy = ResourceFactory.createProperty(PrefixEARL +
    "assertedBy")
  val PropertyEARLTest = ResourceFactory.createProperty(PrefixEARL + "test")

  def main(args: Array[String]): Unit = {

  }
}