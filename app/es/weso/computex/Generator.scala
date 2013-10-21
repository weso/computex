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
import PREFIXES._
import com.hp.hpl.jena.rdf.model.ResourceFactory

/**
 * Generates a WebIndex example file with random values
 */
case class Generator(
    val NumCountries: Int, 
    val NumIndicators: Int,
    val NumYears: Int) {

   val m = ModelFactory.createDefaultModel()
   m.setNsPrefixes(PREFIXES.cexMapping)

   val indicatorWeights			= ResourceFactory.createResource(PREFIXES.wi_weightSchema + "indicatorWeights")
   val index_index				= ResourceFactory.createProperty(PREFIXES.wi_index 	+ "index")


   val literalTrue				= m.createTypedLiteral("true",XSDDatatype.XSDboolean)

   def literalInt(i : Int) 		= m.createTypedLiteral(new Integer(i))
   def literalFloat(n : Float) 	= m.createTypedLiteral(n.toString,XSDDatatype.XSDfloat)
   def literal(name: String) 	= m.createLiteral(name)

  private def indicator(name: String) : Resource = {
    m.createResource(PREFIXES.wi_indicator + name)
  }

  private def dataSetRaw(name: String) : Resource = {
    m.createResource(PREFIXES.wi_dataset + name + "-Raw")
  }

  private def dataSetImputed(name: String) : Resource = {
    m.createResource(PREFIXES.wi_dataset + name + "-Imputed")
  }

  private def dataSetNormalized(name: String) : Resource = {
    m.createResource(PREFIXES.wi_dataset + name + "-Normalized")
  }

  private def dataSetAdjusted : Resource = {
    m.createResource(PREFIXES.wi_dataset + "Adjusted")
  }

  private def dataSetComposite : Resource = {
    m.createResource(PREFIXES.wi_dataset + "Composite")
  }

  private def dataSetRanking : Resource = {
    m.createResource(PREFIXES.wi_dataset + "Ranking")
  }

  private def country(name: String) : Resource = {
    m.createResource(PREFIXES.wi_country + name)
  }

  private def observation(indicator: String, year: String, country: String) : Resource = {
    m.createResource(PREFIXES.wi_obs + indicator + year + country)
  }


  private def sliceIndicatorYear(indicator: String, year: String, kind: String) : Resource = {
    m.createResource(PREFIXES.wi_slice + indicator + year + "-" + kind)
  }

  private def slice_composite : Resource = {
    m.createResource(PREFIXES.wi_slice + "composite")
  } 

  private def slice_ranking : Resource = {
    m.createResource(PREFIXES.wi_slice + "ranking")
  } 

  lazy val indicatorNames: IndexedSeq[String] = {
    for (i <- 0 to NumIndicators - 1) 
      yield "I" + i
  }

  lazy val yearNames: IndexedSeq[String] = {
    for (i <- 0 to NumYears - 1) 
      yield "Year" + i
  }
  
  private lazy val year : String = 
    yearNames.last

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
    m.add(wf_onto_DSD,rdf_type,qb_DataStructureDefinition)
    val c1 = m.createResource()
    m.add(c1,qb_dimension,wf_onto_ref_area)
    m.add(c1,qb_order,literalInt(1))
    m.add(wf_onto_DSD,qb_component,c1)     

    val c2 = m.createResource()
    m.add(c2,qb_dimension,wf_onto_ref_year)
    m.add(c2,qb_order,literalInt(2))
    m.add(wf_onto_DSD,qb_component,c2)     
    
    val c3 = m.createResource()
    m.add(c3,qb_dimension,cex_indicator)
    m.add(c3,qb_order,literalInt(3))
    m.add(wf_onto_DSD,qb_component,c3)     
    
    val measure = m.createResource()
    m.add(measure,qb_measure,cex_value)
    m.add(wf_onto_DSD,qb_component,measure) 
    
    wf_onto_DSD
  }
  
  private def createSliceByArea(m : Model): Resource = {
    m.add(wf_onto_sliceByArea,rdf_type, qb_SliceKey)
    m.add(wf_onto_sliceByArea,qb_componentProperty, cex_indicator)
    m.add(wf_onto_sliceByArea,qb_componentProperty, wf_onto_ref_year)
    wf_onto_sliceByArea
  }


  def addIndicators : Unit = {
    for (name <- indicatorNames) {
      val ind = indicator(name)
      m.add(ind,rdf_type,wf_onto_SecondaryIndicator)
      m.add(ind,cex_highLow,cex_High)
      m.add(ind,rdfs_label,m.createLiteral("Indicator " + name,"en"))
    }
  }
  
  def addCountries : Unit = {
    for (name <- countryNames) {
      val c = country(name)
      m.add(c,rdf_type,wf_onto_Country)
      m.add(c,rdfs_label,m.createLiteral("Country " + name,"en"))
    }
  }

  def addRanges : Unit = {
    m.add(wf_onto_ref_year,rdfs_range,m.createResource(PREFIXES.wf_onto + "Year"))
    m.add(wf_onto_ref_area,rdfs_range,m.createResource(PREFIXES.wf_onto + "Area"))
    m.add(cex_indicator,rdfs_range,m.createResource(PREFIXES.wf_onto + "Indicator"))
  }
  
  def addDatasets : Unit = {
    for (name <- indicatorNames) {
      val ds = dataSetRaw(name)
      m.add(ds,rdf_type,qb_DataSet)
      m.add(ds,qb_structure,wf_onto_DSD)
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
      m.add(ds,rdf_type,qb_DataSet)
      m.add(ds,qb_structure,wf_onto_DSD)
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
        m.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
        m.add(slice,cex_indicator,indicator(name))      
        m.add(slice,wf_onto_ref_year,literal(yearName))      
        m.add(slice,rdf_type,qb_Slice)      
      }
    }
  }
  
  def addDatasetsNormalized : Unit = {
    for (name <- indicatorNames) {
      val ds = dataSetNormalized(name)
      m.add(ds,rdf_type,qb_DataSet)
      m.add(ds,qb_structure,wf_onto_DSD)
      m.add(ds,rdfs_label,m.createLiteral("Dataset " + name + "-Normalized","en"))
      
      val computation = m.createResource()
      m.add(computation,rdf_type,cex_NormalizeDataSet)
      m.add(computation,cex_dataSet,dataSetImputed(name))
      
      m.add(ds,cex_computation,computation)
      
      for (yearName <- yearNames) {
        val slice = sliceIndicatorYear(name,yearName,"Normalized")
        m.add(ds,qb_slice,slice)
        m.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
        m.add(slice,cex_indicator,indicator(name))      
        m.add(slice,wf_onto_ref_year,literal(yearName))      
        m.add(slice,rdf_type,qb_Slice)      
      }
    }
  }


  def addDatasetsAdjusted : Unit = {
      val ds = dataSetAdjusted
      m.add(ds,rdf_type,qb_DataSet)
      m.add(ds,qb_structure,wf_onto_DSD)
      m.add(ds,rdfs_label,m.createLiteral("DataSet " + "Adjusted","en"))

      val computation = m.createResource()
      m.add(computation,rdf_type,cex_AdjustDataSet)
      m.add(computation,cex_increment,literalInt(8))
      m.add(computation,cex_dimension,wf_onto_ref_year)
      m.add(computation,cex_value,year)
      for (name <- indicatorNames) {
       m.add(computation,cex_dataSet,dataSetNormalized(name))
      }      
      m.add(ds,cex_computation,computation)
      
      for (name <- indicatorNames) {      
        val slice = sliceIndicatorYear(name,year,"Adjusted")
        m.add(ds,qb_slice,slice)
        m.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
        m.add(slice,cex_indicator,indicator(name))
        m.add(slice,wf_onto_ref_year,literal(year))      
        m.add(slice,rdf_type,qb_Slice)      
      }
  }

  def addWeightSchema : Unit = {
    m.add(indicatorWeights,rdf_type,cex_WeightSchema)
    for (name <- indicatorNames) {
     val weight=m.createResource()
     m.add(weight,rdf_type,cex_Weight)
     m.add(weight,cex_dimension,cex_indicator)
     m.add(weight,cex_element,indicator(name))
     m.add(weight,cex_value,literalFloat(1 / indicatorNames.length.toFloat))
     m.add(indicatorWeights,cex_weight,weight)
    }
  }

  def addDatasetComposite : Unit = {
    val composite = dataSetComposite
    m.add(composite,rdf_type,qb_DataSet)
    m.add(composite,qb_structure,wf_onto_DSD)
    m.add(composite,rdfs_label,m.createLiteral("Dataset " + "Composite","en"))
    val computation = m.createResource()
    m.add(computation,rdf_type,cex_WeightedMean)
    m.add(computation,cex_dataSet,dataSetAdjusted)
    m.add(computation,cex_component,index_index)
    m.add(computation,cex_dimension,wf_onto_ref_area)
    m.add(computation,cex_weightSchema,indicatorWeights)
    m.add(composite,cex_computation,computation)
    val slice = slice_composite
    m.add(composite,qb_slice,slice)
    m.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
    m.add(slice,cex_indicator,index_index)
    m.add(slice,wf_onto_ref_year,year)      
    m.add(slice,rdf_type,qb_Slice)
    
    m.add(index_index,rdf_type,cex_index)
    for (name <- indicatorNames) {
      m.add(index_index,cex_element,indicator(name))
    }
    
  }

  def addDatasetRanking : Unit = {
    val ranking = dataSetRanking
    m.add(ranking,rdf_type,qb_DataSet)
    m.add(ranking,qb_structure,wf_onto_DSD)
    m.add(ranking,rdfs_label,m.createLiteral("Dataset " + "Ranking","en"))
    val computation = m.createResource()
    m.add(computation,rdf_type,cex_Ranking)
    m.add(computation,cex_slice,slice_composite)
    m.add(computation,cex_dimension,wf_onto_ref_area)
    m.add(ranking,cex_computation,computation)
    val slice = slice_ranking
    m.add(ranking,qb_slice,slice)
    m.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
    m.add(slice,cex_indicator,index_index)
    m.add(slice,wf_onto_ref_year,year)      
    m.add(slice,rdf_type,qb_Slice)
  }

  def addSlices : Unit = {
    for (indic <- indicatorNames; year <- yearNames) {
      val slice = sliceIndicatorYear(indic, year,"Raw")
      
      m.add(slice,rdf_type,qb_Slice)
      m.add(slice,cex_indicator,indicator(indic))
      m.add(slice,wf_onto_ref_year,literal(year))
      m.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
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
      
      m.add(obs,rdf_type,qb_Observation)
      m.add(obs,wf_onto_ref_area,country(c))
      m.add(obs,cex_indicator,indicator(i))
      m.add(obs,wf_onto_ref_year,literal(y))
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
    addDatasetsAdjusted
    addWeightSchema
    addDatasetComposite 
    addDatasetRanking 
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
    val doValidation = 
    			toggle("validate",
    			    default=Some(false),
    				descrYes = "Validate model using Computex",
    				descrNo  = "Do not validate")
    val doComputation = 
    			toggle("compute",
    				default=Some(false),
    				descrYes = "Apply computation steps",
    				descrNo  = "Don't compute")
    val expand  = opt[Boolean]("expand",
    				default=Some(false),
    				descr = "Expand model using Computex")
    val show  	= toggle("show",
    				default=Some(true),
    				descrYes = "Show model generated",
    				descrNo = "Do not show model generated")
    val showTime	= toggle("time",
    				default=Some(true),
    				descrYes = "Show time",
    				descrNo = "Do not show time")
    val output  = opt[String]("out",
    				default=Some(""),
    				descr = "Output model to file")
    val verbose = toggle("Verbose", 
    				default=Some(false),
    				descrYes = "Verbose output",
    				descrNo = "Not verbose"
    				)
    val statistics = toggle("statistics", 
    				default=Some(false),
    				descrYes = "Show statistics",
    				descrNo = "Do not show statistics"
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

      val startTime = System.nanoTime
	  val profile = Profile.Computex
	  var outputModel = gen.model
	  if (opts.doComputation()) {
	    println("Applying computation steps to generated model...")
	    val model = profile.expand(gen.model)
	    outputModel = model
	  }

	  if (opts.doValidation()) {
	    println("Validating generated model...")
	    val (vr,model) = profile.validate(outputModel,opts.expand(),true)
	    println("After validating generated model...")
	    println(VReport.show(vr,opts.verbose()))
	    outputModel = model
	  }

      if (opts.showTime()) {
        println("Total time: " + (System.nanoTime-startTime)/1e6+"ms")
      }

	  if (opts.show()) {
		showOutputModel(opts.output(),outputModel)	    
	  }

	  if (opts.statistics()) {
	    println("Size of model:" + outputModel.size)
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
    
    

