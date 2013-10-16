package es.weso.computex

import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl
import scalaj.collection.Imports._

object PREFIXES {
  lazy val webindex 	= "http://data.webfoundation.org/webindex/v2013/"
  lazy val computation	= webindex + "computation/"
  lazy val dataset		= webindex + "dataset/"
  lazy val index		= webindex + "index/"
  lazy val indicator	= webindex + "indicator/"
  lazy val country		= webindex + "country/"
  lazy val obs			= webindex + "observation/"
  lazy val ranking		= webindex + "ranking/"
  lazy val slice		= webindex + "slice/"
  lazy val weightSchema	= webindex + "weightSchema/"
  lazy val cex			= "http://purl.org/weso/ontology/computex#"
  lazy val dcterms		= "http://purl.org/dc/terms/"
  lazy val geo			= "http://www.w3.org/2003/01/geo/wgs84_pos#"
  lazy val rdf 			= "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  lazy val qb  			= "http://purl.org/linked-data/cube#"
  lazy val owl 			= "http://www.w3.org/2002/07/owl#"
  lazy val wf_onto		= "http://data.webfoundation.org/ontology/"
  lazy val rdfs			= "http://www.w3.org/2000/01/rdf-schema#"
  lazy val sdmxAttribute = "http://purl.org/linked-data/sdmx/2009/attribute#"
  lazy val sdmxCode		= "http://purl.org/linked-data/sdmx/2009/code#"
  lazy val sdmxConcept	= "http://purl.org/linked-data/sdmx/2009/concept#"
  lazy val sdmxSubject	= "http://purl.org/linked-data/sdmx/2009/subject#"
  lazy val skos			= "http://www.w3.org/2004/02/skos/core#"
  lazy val time			= "http://www.w3.org/2006/time#"
  lazy val wf_org		= "http://data.webfoundation.org/organization/"
  lazy val wf_people	= "http://data.webfoundation.org/people/"
  lazy val xsd			= "http://www.w3.org/2001/XMLSchema#"

  private val cexMap : Map[String,String] = 
    	Map("webindex" 		-> webindex,
		    "computation" 	-> computation,
		    "ranking" 		-> ranking,
		    "indicator"		-> indicator,
		    "country"		-> country,
		    "dataset"		-> dataset,
		    "obs"			-> obs, 
		    "slice"			-> slice,
		    "cex"			-> cex,
		    "dcterms"		-> dcterms,
		    "index"			-> index,
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
		    "weightSchema"	-> weightSchema,
		    "wf-onto"		-> wf_onto,
		    "wf-org"		-> wf_org,
		    "wf-people"		-> wf_people,
		    "xsd"			-> xsd
    )

    val cexMapping = new PrefixMappingImpl()
    
    cexMapping.setNsPrefixes(cexMap.asJava)
    
}