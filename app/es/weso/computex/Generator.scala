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
import org.slf4j.LoggerFactory
import org.rogach.scallop.Scallop
import org.rogach.scallop.ScallopConf
import org.rogach.scallop.exceptions.Help
import es.weso.utils.JenaUtils
import es.weso.computex.profile.Profile
import es.weso.computex.profile.VReport

/**
 * Generates a WebIndex example file with random values
 */
case class Generator(
    val NumCountries: Int, 
    val NumIndicators: Int,
    val NumYears: Int) {

   val m = ModelFactory.createDefaultModel()
   m.setNsPrefixes(PREFIXES.cexMapping)

   val rdf_type 				= m.createProperty(PREFIXES.rdf 	+ "type")
   val rdfs_label				= m.createProperty(PREFIXES.rdfs 	+ "label")
   val rdfs_comment				= m.createProperty(PREFIXES.rdfs 	+ "comment")
   val rdfs_range				= m.createProperty(PREFIXES.rdfs 	+ "range")

   val cex_indicator			= m.createProperty(PREFIXES.cex 	+ "indicator")
   val cex_value				= m.createProperty(PREFIXES.cex 	+ "value")
   val cex_computation			= m.createProperty(PREFIXES.cex 	+ "computation")
   val cex_dataSet				= m.createProperty(PREFIXES.cex 	+ "dataSet")
   val cex_highLow				= m.createProperty(PREFIXES.cex 	+ "highLow")
   val cex_method				= m.createProperty(PREFIXES.cex 	+ "method")
   val cex_ImputeDataSet		= m.createProperty(PREFIXES.cex 	+ "ImputeDataSet")
   val cex_NormalizeDataSet		= m.createProperty(PREFIXES.cex 	+ "NormalizeDataSet")
   val cex_MeanBetweenMissing	= m.createProperty(PREFIXES.cex 	+ "MeanBetweenMissing")
   val cex_AvgGrowth2Missing	= m.createProperty(PREFIXES.cex 	+ "AvgGrowth2Missing")
   val cex_CopyRaw				= m.createProperty(PREFIXES.cex 	+ "CopyRaw")
   val cex_High					= m.createProperty(PREFIXES.cex 	+ "High")

   val qb_attribute 			= m.createProperty(PREFIXES.qb 		+ "attribute")
   val qb_dataSet	 			= m.createProperty(PREFIXES.qb 		+ "dataSet")
   val qb_component 			= m.createProperty(PREFIXES.qb 		+ "component")
   val qb_componentRequired		= m.createProperty(PREFIXES.qb 		+ "componentRequired")
   val qb_componentAttachment	= m.createProperty(PREFIXES.qb 		+ "componentAttachment")
   val qb_componentProperty		= m.createProperty(PREFIXES.qb 		+ "componentProperty")
   val qb_dimension			 	= m.createProperty(PREFIXES.qb 		+ "dimension")
   val qb_measure 				= m.createProperty(PREFIXES.qb 		+ "measure")
   val qb_order				 	= m.createProperty(PREFIXES.qb 		+ "order")
   val qb_observation		 	= m.createProperty(PREFIXES.qb 		+ "observation")
   val qb_slice					= m.createProperty(PREFIXES.qb 		+ "slice")
   val qb_sliceKey				= m.createProperty(PREFIXES.qb 		+ "sliceKey")
   val qb_sliceStructure		= m.createProperty(PREFIXES.qb 		+ "sliceStructure")
   val qb_structure				= m.createProperty(PREFIXES.qb 		+ "structure")
   
   val qbDataStructureDefinition = m.createResource(PREFIXES.qb 	+ "DataStructureDefinition")
   val qbDataset				= m.createResource(PREFIXES.qb 		+ "DataSet")
   val qbSlice					= m.createResource(PREFIXES.qb 		+ "Slice")
   val qbSliceKey				= m.createResource(PREFIXES.qb 		+ "SliceKey")
   val qbObservation			= m.createResource(PREFIXES.qb 		+ "Observation")
   

   val wi_ontoDSD				= m.createResource(PREFIXES.wi_onto + "DSD")
   val sliceByArea 				= m.createResource(PREFIXES.wi_onto + "sliceByArea")
   
   val wi_onto_ref_area 		= m.createProperty(PREFIXES.wi_onto + "ref-area")
   val wi_onto_ref_year 		= m.createProperty(PREFIXES.wi_onto + "ref-year")
   val wi_onto_sliceByArea 		= m.createProperty(PREFIXES.wi_onto + "sliceByArea")
   val wi_ontoCountry 			= m.createResource(PREFIXES.wi_onto + "Country")
   val wi_ontoSecondaryIndicator = m.createResource(PREFIXES.wi_onto + "SecondaryIndicator")
   val sdmxUnitMeasure			= m.createResource(PREFIXES.sdmxAttribute + "unitMeasure") 

   val literalTrue				= m.createTypedLiteral("true",XSDDatatype.XSDboolean)

   def literalInt(i : Int) 		= m.createTypedLiteral(new Integer(i))
   def literalFloat(n : Float) 	= m.createTypedLiteral(n.toString,XSDDatatype.XSDfloat)
   def literal(name: String) 	= m.createLiteral(name)

  private def indicator(name: String) : Resource = {
    m.createResource(PREFIXES.indicator + name)
  }

  private def dataSetRaw(name: String) : Resource = {
    m.createResource(PREFIXES.dataset + name + "-Raw")
  }

  private def dataSetImputed(name: String) : Resource = {
    m.createResource(PREFIXES.dataset + name + "-Imputed")
  }

  private def dataSetNormalized(name: String) : Resource = {
    m.createResource(PREFIXES.dataset + name + "-Normalized")
  }

  private def country(name: String) : Resource = {
    m.createResource(PREFIXES.indicator + name)
  }

  private def observation(indicator: String, year: String, country: String) : Resource = {
    m.createResource(PREFIXES.obs + indicator + year + country)
  }


  private def sliceIndicatorYear(indicator: String, year: String, kind: String) : Resource = {
    m.createResource(PREFIXES.slice + indicator + year + "-" + kind)
  }

  lazy val indicatorNames: IndexedSeq[String] = {
    for (i <- 0 to NumIndicators - 1) 
      yield "I" + i
  }

  lazy val yearNames: IndexedSeq[String] = {
    for (i <- 0 to NumYears - 1) 
      yield "Year" + i
  }

  lazy val countryNames: IndexedSeq[String] = {
    for (i <- 0 to NumCountries - 1) 
      yield "C" + i
  }
  
  lazy val values: Vector[Vector[Vector[Float]]] = {
   Vector.tabulate(NumCountries, NumIndicators, NumYears)((c,i,y) => Random.nextFloat()) 
  }
  
  def getValue(
		  country: String, 
		  indicator: String,
		  year: String
		  ) : Float = {
    values(getCountryIndex(country))(getIndicatorIndex(indicator))(getYearIndex(year))
  }
  
  def getCountryIndex(country: String) : Int = 
   countryNames.indexOf(country)
  
  def getYearIndex(year: String) : Int = 
   	yearNames.indexOf(year)
  
  def getIndicatorIndex(indicator: String) : Int = 
    indicatorNames.indexOf(indicator)

  def showValues : String = {
   val is = for (i <- countryNames)
	   		yield (i + " " )
   val vs = for (c <- countryNames;i <- indicatorNames; y <- yearNames)
            yield ("C: " + c + ", I: " + i + ", Y: " + y + ", V: " + getValue(c,i,y) + "\n")
   is.mkString + "\n" + vs.mkString
   
  }

  def write(out: OutputStream, 
		    syntax: String = "TURTLE") : Unit = {
   model.write(out,syntax) 
  }
  
  def writeFile(fileName: String, 
		    	syntax: String = "TURTLE") : Unit = {
    model.write(new FileOutputStream(fileName),syntax) 
  }

  def createDSD(m : Model): Resource = {
    m.add(wi_ontoDSD,rdf_type,qbDataStructureDefinition)
    val c1 = m.createResource()
    m.add(c1,qb_dimension,wi_onto_ref_area)
    m.add(c1,qb_order,literalInt(1))
    m.add(wi_ontoDSD,qb_component,c1)     

    val c2 = m.createResource()
    m.add(c2,qb_dimension,wi_onto_ref_year)
    m.add(c2,qb_order,literalInt(2))
    m.add(wi_ontoDSD,qb_component,c2)     
    
    val c3 = m.createResource()
    m.add(c3,qb_dimension,cex_indicator)
    m.add(c3,qb_order,literalInt(3))
    m.add(wi_ontoDSD,qb_component,c3)     
    
    val measure = m.createResource()
    m.add(measure,qb_measure,cex_value)
    m.add(wi_ontoDSD,qb_component,measure) 
    
    wi_ontoDSD
  }
  
  private def createSliceByArea(m : Model): Resource = {
    m.add(sliceByArea,rdf_type, qbSliceKey)
    m.add(sliceByArea,qb_componentProperty, cex_indicator)
    m.add(sliceByArea,qb_componentProperty, wi_onto_ref_year)
    sliceByArea
  }


  def addIndicators : Unit = {
    for (name <- indicatorNames) {
      val ind = indicator(name)
      m.add(ind,rdf_type,wi_ontoSecondaryIndicator)
      m.add(ind,cex_highLow,cex_High)
      m.add(ind,rdfs_label,m.createLiteral("Indicator " + name,"en"))
    }
  }
  
  def addCountries : Unit = {
    for (name <- countryNames) {
      val c = country(name)
      m.add(c,rdf_type,wi_ontoCountry)
      m.add(c,rdfs_label,m.createLiteral("Country " + name,"en"))
    }
  }

  def addRanges : Unit = {
    m.add(wi_onto_ref_year,rdfs_range,m.createResource(PREFIXES.wi_onto + "Year"))
    m.add(wi_onto_ref_area,rdfs_range,m.createResource(PREFIXES.wi_onto + "Area"))
    m.add(cex_indicator,rdfs_range,m.createResource(PREFIXES.wi_onto + "Indicator"))
  }
  
  def addDatasets : Unit = {
    for (name <- indicatorNames) {
      val ds = dataSetRaw(name)
      m.add(ds,rdf_type,qbDataset)
      m.add(ds,qb_structure,wi_ontoDSD)
      m.add(ds,rdfs_label,m.createLiteral("Dataset " + name + "-Raw","en"))
      for (yearName <- yearNames) {
        val slice = sliceIndicatorYear(name,yearName,"Raw")
        m.add(ds,qb_slice,slice)        
      }
    }
  }
  
  def addDatasetsImputed : Unit = {
    for (name <- indicatorNames) {
      val ds = dataSetImputed(name)
      m.add(ds,rdf_type,qbDataset)
      m.add(ds,qb_structure,wi_ontoDSD)
      m.add(ds,rdfs_label,m.createLiteral("Dataset " + name + "-Imputed","en"))
      
      val computation = m.createResource()
      m.add(computation,rdf_type,cex_ImputeDataSet)
      m.add(computation,cex_method,cex_CopyRaw)
      m.add(computation,cex_method,cex_AvgGrowth2Missing)
      m.add(computation,cex_method,cex_MeanBetweenMissing)
      m.add(computation,cex_dataSet,dataSetRaw(name))
      
      m.add(ds,cex_computation,computation)
      
      for (yearName <- yearNames) {
        val slice = sliceIndicatorYear(name,yearName,"Imputed")
        m.add(ds,qb_slice,slice)
        m.add(slice,qb_sliceStructure,wi_onto_sliceByArea)
        m.add(slice,cex_indicator,indicator(name))      
        m.add(slice,wi_onto_ref_year,literal(yearName))      
        m.add(slice,rdf_type,qbSlice)      
      }
    }
  }
  
  def addDatasetsNormalized : Unit = {
    for (name <- indicatorNames) {
      val ds = dataSetNormalized(name)
      m.add(ds,rdf_type,qbDataset)
      m.add(ds,qb_structure,wi_ontoDSD)
      m.add(ds,rdfs_label,m.createLiteral("Dataset " + name + "-Normalized","en"))
      
      val computation = m.createResource()
      m.add(computation,rdf_type,cex_NormalizeDataSet)
      m.add(computation,cex_dataSet,dataSetImputed(name))
      
      m.add(ds,cex_computation,computation)
      
      for (yearName <- yearNames) {
        val slice = sliceIndicatorYear(name,yearName,"Normalized")
        m.add(ds,qb_slice,slice)
        m.add(slice,qb_sliceStructure,wi_onto_sliceByArea)
        m.add(slice,cex_indicator,indicator(name))      
        m.add(slice,wi_onto_ref_year,literal(yearName))      
        m.add(slice,rdf_type,qbSlice)      
      }
    }
  }

  def addSlices : Unit = {
    for (indic <- indicatorNames; year <- yearNames) {
      val slice = sliceIndicatorYear(indic, year,"Raw")
      
      m.add(slice,rdf_type,qbSlice)
      m.add(slice,cex_indicator,indicator(indic))
      m.add(slice,wi_onto_ref_year,literal(year))
      m.add(slice,qb_sliceStructure,wi_onto_sliceByArea)
      for (country <- countryNames) {
        m.add(slice,qb_observation,observation(indic,year,country))
      }
    }
  }

  def addObservations : Unit = {
    for (i <- indicatorNames; 
    	 y <- yearNames;
    	 c <- countryNames) {
      
      val obs = observation(i,y,c)
      
      m.add(obs,rdf_type,qbObservation)
      m.add(obs,wi_onto_ref_area,country(c))
      m.add(obs,cex_indicator,indicator(i))
      m.add(obs,wi_onto_ref_year,literal(y))
      m.add(obs,qb_dataSet,dataSetRaw(i))
      m.add(obs,cex_value,literalFloat(getValue(c,i,y)))
      
    }
  }

  lazy val model : Model = {
    val dsd = createDSD(m)
    val sliceByArea = createSliceByArea(m)
    m.add(dsd,qb_sliceKey,sliceByArea)
    addIndicators
    addCountries
    addRanges
    addDatasets
    addSlices
    addObservations
    addDatasetsImputed
    addDatasetsNormalized
    m
  }
  
  def showModel(syntax : String = "TURTLE") : String = {
    JenaUtils.model2Str(model,syntax)
  }
}


class GeneratorOpts(arguments: Array[String],onError: (Throwable, Scallop) => Nothing
    ) extends ScallopConf(arguments) {

    banner("""| Generate WebIndex sample data
              | Options:
              |""".stripMargin)
    footer("Enjoy!")
    version("Computex Generator 0.1")
    val countries = opt[Int]("countries",
    				default=Some(1),
    				descr = "Number of countries")
    val indicators = opt[Int]("indicators", 
        			default=Some(1),
    				descr = "Number of indicators")        			
    val years      = opt[Int]("years", 
        			default=Some(1),
    				descr = "Number of years")        			
    val doValidation = opt[Boolean]("validate",
    				default=Some(false),
    				descr = "Validate model using Computex")
    val expand  = opt[Boolean]("expand",
    				default=Some(false),
    				descr = "Expand model using Computex")
    val show  	= toggle("show",
    				default=Some(true),
    				descrYes = "Show model generated",
    				descrNo = "Do not show model generated")
    val output  = opt[String]("out",
    				default=Some(""),
    				descr = "Output model to file")
    val verbose = toggle("Verbose", 
    				default=Some(false),
    				descrYes = "Verbose output",
    				descrNo = "Not verbose"
    				)
    val version = opt[Boolean]("version", 
    				noshort = true, 
    				descr = "Print version")
    val help 	= opt[Boolean]("help", 
    				noshort = true, 
    				descr = "Show this message")
  
  override protected def onError(e: Throwable) = onError(e, builder)
}


object Generator extends App {

  override def main(args: Array[String]) {
	  val logger 		= LoggerFactory.getLogger("Application")
	  val conf 			= ConfigFactory.load()
      val opts 			= new GeneratorOpts(args,onError)

	  val gen = new Generator(opts.countries(),opts.indicators(),opts.years())

	  val profile = Profile.Computex
	  var outputModel = gen.model
	  if (opts.doValidation()) {
	    println("Validating generated model...")
	    val (vr,model) = profile.validate(gen.model,opts.expand(),true)
	    println("After validating generated model...")
	    println(VReport.show(vr,opts.verbose()))
	    outputModel = model
	  }

	  if (opts.show()) {
		showOutputModel(opts.output(),outputModel)	    
	  }

  }
  
  def showOutputModel(output: String, model:Model) {
    output match {
	    case "" => println(JenaUtils.model2Str(model))
	    case str => JenaUtils.model2File(model, str)
	  }
  }   
  
  private def onError(e: Throwable, scallop: Scallop) = e match {
    case Help(s) =>
      println("Help: " + s)
      scallop.printHelp
      sys.exit(0)
    case _ =>
      println("Error: %s".format(e.getMessage))
      scallop.printHelp
      sys.exit(1)
  }

}
    
    

