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

/**
 * Generates a WebIndex example file with random values
 */
case class Generator(
    val NumCountries: Int, 
    val NumIndicators: Int,
	val outputStream: OutputStream) {
  
  lazy val indicatorNames: IndexedSeq[String] = {
    for (i <- 0 to NumIndicators - 1) 
      yield "I" + i
  }

  lazy val countryNames: IndexedSeq[String] = {
    for (i <- 0 to NumCountries - 1) 
      yield "C" + i
  }
  
  lazy val values: Vector[Vector[Float]] = {
   Vector.tabulate(NumCountries, NumIndicators)((c,i) => Random.nextFloat()) 
  }
  
  def getValue(
		  country: String, 
		  indicator: String
		  ) : Float = {
    values(getCountryIndex(country))(getIndicatorIndex(indicator))
  }
  
  def getCountryIndex(country: String) : Int = 
   countryNames.indexOf(country)
  
  
  def getIndicatorIndex(indicator: String) : Int = 
    indicatorNames.indexOf(indicator)

  def showValues : String = {
   val is = for (i <- countryNames)
	   		yield (i + " " )
   val vs = for (c <- countryNames;i <- indicatorNames)
            yield ("C: " + c + ", I: " + i + ", V: " + getValue(c,i) + "\n")
   is.mkString + "\n" + vs.mkString
   
  }
  
  lazy val model : Model = {
    val m = ModelFactory.createDefaultModel()
    val rdftype = m.createProperty(PREFIXES.rdf + "type")
    val dataStructureDefinition = m.createResource(PREFIXES.qb + "DataStructureDefinition")
    val dsd = m.createResource(PREFIXES.wi_onto + "DSD")
    m.add(dsd,rdftype,dataStructureDefinition)
    m
  }
}

object Generator {

  
}
    
    

