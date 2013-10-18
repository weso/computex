package es.weso.computex

import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl
import scalaj.collection.Imports._
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.weso.utils.JenaUtils

object PREFIXES {
  lazy val webindex 	= "http://data.webfoundation.org/webindex/v2013/"
  lazy val odb		 	= "http://data.webfoundation.org/odb/v2013/"
  lazy val test		 	= "http://data.webfoundation.org/test/v2013/"
  lazy val wi_computation	= webindex + "computation/"
  lazy val wi_component		= webindex + "component/"
  lazy val wi_dataset		= webindex + "dataset/"
  lazy val wi_index			= webindex + "index/"
  lazy val wi_indicator		= webindex + "indicator/"
  lazy val wi_country		= webindex + "country/"
  lazy val wi_obs			= webindex + "observation/"
  lazy val wi_ranking		= webindex + "ranking/"
  lazy val wi_slice			= webindex + "slice/"
  lazy val wi_weightSchema	= webindex + "weightSchema/"
  lazy val odb_computation	= odb + "computation/"
  lazy val odb_component	= odb + "component/"
  lazy val odb_dataset		= odb + "dataset/"
  lazy val odb_index		= odb + "index/"
  lazy val odb_indicator	= odb + "indicator/"
  lazy val odb_country		= odb + "country/"
  lazy val odb_obs			= odb + "observation/"
  lazy val odb_ranking		= odb + "ranking/"
  lazy val odb_slice		= odb + "slice/"
  lazy val odb_weightSchema	= odb + "weightSchema/"
  lazy val cex			= "http://purl.org/weso/ontology/computex#"
  lazy val dcterms		= "http://purl.org/dc/terms/"
  lazy val geo			= "http://www.w3.org/2003/01/geo/wgs84_pos#"
  lazy val rdf 			= "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  lazy val qb  			= "http://purl.org/linked-data/cube#"
  lazy val owl 			= "http://www.w3.org/2002/07/owl#"
  lazy val rdfs			= "http://www.w3.org/2000/01/rdf-schema#"
  lazy val sdmxAttribute = "http://purl.org/linked-data/sdmx/2009/attribute#"
  lazy val sdmxCode		= "http://purl.org/linked-data/sdmx/2009/code#"
  lazy val sdmxConcept	= "http://purl.org/linked-data/sdmx/2009/concept#"
  lazy val sdmxSubject	= "http://purl.org/linked-data/sdmx/2009/subject#"
  lazy val skos			= "http://www.w3.org/2004/02/skos/core#"
  lazy val time			= "http://www.w3.org/2006/time#"
  lazy val wf_onto		= "http://data.webfoundation.org/ontology/"
  lazy val wf_org		= "http://data.webfoundation.org/organization/"
  lazy val wf_people	= "http://data.webfoundation.org/people/"
  lazy val xsd			= "http://www.w3.org/2001/XMLSchema#"

  private val cexMap : Map[String,String] = 
    	Map("webindex" 			-> webindex,
		    "odb"				-> odb,
    	    "test"				-> test,
		    "wi-country"		-> wi_country,
    	    "wi-computation" 	-> wi_computation,
    	    "wi-component" 		-> wi_component,
		    "wi-index"			-> wi_index,
		    "wi-indicator"		-> wi_indicator,
		    "wi-dataset"		-> wi_dataset,
		    "wi-obs"			-> wi_obs, 
		    "wi-ranking" 		-> wi_ranking,
		    "wi-slice"			-> wi_slice,
		    "wi-weightSchema"	-> wi_weightSchema,
    	    "odb-computation" 	-> odb_computation,
    	    "odb-component" 	-> odb_component,
		    "odb-ranking" 		-> odb_ranking,
		    "odb-indicator"		-> odb_indicator,
		    "odb-country"		-> odb_country,
		    "odb-dataset"		-> odb_dataset,
		    "odb-obs"			-> odb_obs, 
		    "odb-slice"			-> odb_slice,
		    "odb-index"			-> odb_index,
		    "odb-weightSchema"	-> odb_weightSchema,
		    "cex"			-> cex,
		    "dcterms"		-> dcterms,
		    "geo"			-> geo,
		    "qb"			-> qb,
		    "owl"			-> owl,
		    "rdf"			-> rdf,
		    "rdfs"			-> rdfs,
		    "sdmxAttribute" -> sdmxAttribute,
		    "sdmxCode"		-> sdmxCode,
		    "sdmxConcept"	-> sdmxConcept,
		    "sdmxSubject"	-> sdmxSubject,
		    "skos"			-> skos,
		    "time"			-> time,
		    "wf-onto"		-> wf_onto,
		    "wf-org"		-> wf_org,
		    "wf-people"		-> wf_people,
		    "xsd"			-> xsd
    )

    val cexMapping = new PrefixMappingImpl()
    
    cexMapping.setNsPrefixes(cexMap.asJava)
    
    def prefixTemplateTurtle(): String = {
      val strBuilder = new StringBuilder
      for (p <- cexMap.toList.sortBy(_._1)) {
        strBuilder.append("@prefix " + p._1 + ": " + "<" + p._2 + "> .\n") 
      }
      strBuilder.toString
    }
  
    def prefixTemplateSparql(): String = {
      val strBuilder = new StringBuilder
      for (p <- cexMap.toList.sortBy(_._1)) {
        strBuilder.append("PREFIX " + p._1 + ": " + "<" + p._2 + "> \n") 
      }
      strBuilder.toString
    }
}


// Utilities to generate Prefixes in SPARQL and TURTLE
object PrefixesTurtle extends App {
  
  override def main(args: Array[String]) {
    println(PREFIXES.prefixTemplateTurtle)    
  }
  
}

object PrefixesSparql extends App {
  
  override def main(args: Array[String]) {
    println(PREFIXES.prefixTemplateSparql)    
  }
  
}