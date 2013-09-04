package es.weso.computex

import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl
import scalaj.collection.Imports._

object PREFIXES {
  val webindex = "http://data.webfoundation.org/webindex/v2013/"
  val rdf 			= "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val qb  			= "http://purl.org/linked-data/cube#"
  val wi_onto		= "http://data.webfoundation.org/webindex/ontology/"
  val nsMap : Map[String,String] = 
    	Map("webindex" 		-> webindex,
		    "computation" 	-> ( webindex + "computation/" ),
		    "ranking" 		-> ( webindex + "ranking/" ),
		    "indicator"		-> ( webindex + "indicator/" ),
		    "country"		-> ( webindex + "country/" ),
		    "obs"			-> (webindex + "observation/"), 
		    "slice"			-> (webindex + "slice/"),
		    "cex"			-> "http://purl.org/weso/ontology/computex#",
		    "dcterms"		-> "http://purl.org/dc/terms/",
		    "geo"			-> "http://www.w3.org/2003/01/geo/wgs84_pos#",
		    "qb"			-> qb,
		    "owl"			-> "http://www.w3.org/2002/07/owl#",
		    "rdf"			-> rdf,
		    "rdfs"			-> "http://www.w3.org/2000/01/rdf-schema#",
		    "sdmxAttribute" -> "http://purl.org/linked-data/sdmx/2009/attribute#",
		    "sdmxCode"		-> "http://purl.org/linked-data/sdmx/2009/code#",
		    "sdmxConcept"	-> "http://purl.org/linked-data/sdmx/2009/concept#",
		    "sdmxSubject"	-> "http://purl.org/linked-data/sdmx/2009/subject#",
		    "skos"			-> "http://www.w3.org/2004/02/skos/core#",
		    "time"			-> "http://www.w3.org/2006/time#",
		    "wi-onto"		-> wi_onto,
		    "wi-org"		-> "http://data.webfoundation.org/webindex/organization/",
		    "wi-people"		-> "http://data.webfoundation.org/webindex/people/",
		    "xsd"			-> "http://www.w3.org/2001/XMLSchema#"
    )
    val prefixMap = new PrefixMappingImpl()
    
    prefixMap.setNsPrefixes(nsMap.asJava)
    
}