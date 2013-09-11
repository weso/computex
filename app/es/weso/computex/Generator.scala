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

  private def dataSet(name: String) : Resource = {
    m.createResource(PREFIXES.dataset + name + "-Raw")
  }

  private def country(name: String) : Resource = {
    m.createResource(PREFIXES.indicator + name)
  }

  private def observation(indicator: String, year: String, country: String) : Resource = {
    m.createResource(PREFIXES.obs + indicator + year + country)
  }


  private def sliceIndicatorYear(indicator: String, year: String) : Resource = {
    m.createResource(PREFIXES.slice + indicator + year + "-Raw")
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
      val ds = dataSet(name)
      m.add(ds,rdf_type,qbDataset)
      m.add(ds,qb_structure,wi_ontoDSD)
      m.add(ds,rdfs_label,m.createLiteral("Dataset " + name + "-Raw","en"))
      for (yearName <- yearNames) {
        val slice = sliceIndicatorYear(name,yearName)
        m.add(ds,qb_slice,slice)        
      }
    }
  }
  

  def addSlices : Unit = {
    for (indic <- indicatorNames; year <- yearNames) {
      val slice = sliceIndicatorYear(indic, year)
      
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
      m.add(obs,qb_dataSet,dataSet(i))
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
    m
  }
  
  def showModel(syntax : String = "TURTLE") : String = {
    val sw = new StringWriter
    model.write(sw,syntax)
    sw.toString()
  }
}

object Generator {

}
    
    

