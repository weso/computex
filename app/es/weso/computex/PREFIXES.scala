package es.weso.computex

import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl
import scalaj.collection.Imports._
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.weso.utils.JenaUtils
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Resource

object PREFIXES {
  lazy val webindex 	= "http://data.webfoundation.org/webindex/v2013/"
  lazy val odb		 	= "http://data.webfoundation.org/odb/v2013/"
  lazy val test		 	= "http://data.webfoundation.org/test/v2013/"
  lazy val wi_computation	= webindex + "computation/"
  lazy val wi_component		= webindex + "component/"
  lazy val wi_dataset		= webindex + "dataset/"
  lazy val wi_index			= webindex + "index/"
  lazy val wi_indicator		= webindex + "indicator/"
  lazy val wi_region		= webindex + "region/"
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
  lazy val odb_region		= odb + "region/"
  lazy val odb_country		= odb + "country/"
  lazy val odb_obs			= odb + "observation/"
  lazy val odb_ranking		= odb + "ranking/"
  lazy val odb_slice		= odb + "slice/"
  lazy val odb_weightSchema	= odb + "weightSchema/"
  lazy val cex			= "http://purl.org/weso/ontology/computex#"
  lazy val dcterms		= "http://purl.org/dc/terms/"
  lazy val dbpedia		= "http://dbpedia.org/resource/"
  lazy val geo			= "http://www.w3.org/2003/01/geo/wgs84_pos#"
  lazy val rdf 			= "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  lazy val qb  			= "http://purl.org/linked-data/cube#"
  lazy val owl 			= "http://www.w3.org/2002/07/owl#"
  lazy val org 			= "http://www.w3.org/ns/org#"
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
		    "wi-region"			-> wi_region,
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
		    "odb-region"		-> odb_region,
		    "odb-country"		-> odb_country,
		    "odb-dataset"		-> odb_dataset,
		    "odb-obs"			-> odb_obs, 
		    "odb-slice"			-> odb_slice,
		    "odb-index"			-> odb_index,
		    "odb-weightSchema"	-> odb_weightSchema,
		    "cex"			-> cex,
		    "dcterms"		-> dcterms,
		    "dbpedia"		-> dbpedia,
		    "geo"			-> geo,
		    "qb"			-> qb,
		    "owl"			-> owl,
		    "org"			-> org,
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
    
    /**
     * Generates a string with the prefix mapping alphabetically 
     * ordered in Turtle syntax  
     */
    def prefixTemplateTurtle(): String = {
      val strBuilder = new StringBuilder
      for (p <- cexMap.toList.sortBy(_._1)) {
        strBuilder.append("@prefix " + p._1 + ": " + "<" + p._2 + "> .\n") 
      }
      strBuilder.toString
    }
  
    /**
     * Generates a string with the prefix mapping alphabetically 
     * ordered in HTML table syntax  
     */
    def prefixTemplateTable(): String = {
      val strBuilder = new StringBuilder
      for (p <- cexMap.toList.sortBy(_._1)) {
        strBuilder.append("<dt><code>" + p._1 + "</code></dt>" +
                          "<dd><code><a href=\""+ p._2 + "\">" + p._2 + "</a></code></dd>") 
      }
      strBuilder.toString
    }

    /**
     * Generates a string with the prefix mapping alphabetically 
     * ordered in SPARQL syntax  
     */
    def prefixTemplateSparql(): String = {
      val strBuilder = new StringBuilder
      for (p <- cexMap.toList.sortBy(_._1)) {
        strBuilder.append("PREFIX " + p._1 + ": " + "<" + p._2 + "> \n") 
      }
      strBuilder.toString
    }

    def prefixTemplateJSON(): String = {
      val strBuilder = new StringBuilder
      strBuilder.append("{ ")
      val ls = 
        for (p <- cexMap.toList.sortBy(_._1)) 
          yield { "\"" + p._1 + "\":" + "\"" + p._2 + "\"" } 
      
      strBuilder.append(ls.mkString(",\n"))
      strBuilder.append("}")
      strBuilder.toString
    }
    
 lazy val rdf_type 			= property("rdf","type")
 lazy val rdfs_label		= property("rdfs","label")
 lazy val rdfs_comment		= property("rdfs","comment")
 lazy val rdfs_range		= property("rdfs","range")

 lazy val cex_AdjustDataSet	= property("cex","AdjustDataSet")
 lazy val cex_AvgGrowth2Missing = property("cex","AvgGrowth2Missing")
 lazy val cex_AverageGrowth = property("cex","AverageGrowth")
 lazy val cex_Copy			= property("cex","Copy")
 lazy val cex_CopyRaw		= property("cex","CopyRaw")
 lazy val cex_High			= property("cex","High")
 lazy val cex_ImputeDataSet	= property("cex","ImputeDataSet")
 lazy val cex_Imputed	 	= property("cex","Imputed")
 lazy val cex_Indicator 	= property("cex","Indicator")
 lazy val cex_Low			= property("cex","Low")
 lazy val cex_Mean			= property("cex","Mean")
 lazy val cex_MeanBetweenMissing = property("cex","MeanBetweenMissing")
 lazy val cex_Normalize		= property("cex","Normalize")
 
 lazy val cex_Ranking		= property("cex","Ranking")
 lazy val cex_WeightedMean	= property("cex","WeightedMean")
 lazy val cex_WeightSchema	= property("cex","WeightSchema")
 lazy val cex_Weight	= property("cex","Weight")
 lazy val cex_NormalizeDataSet	= property("cex","NormalizeDataSet")
 
 lazy val cex_computation = property("cex","computation")
 lazy val cex_component	= property("cex","component")
 lazy val cex_dataSet	= property("cex","dataSet")
 lazy val cex_dimension	= property("cex","dimension")
 lazy val cex_element	= property("cex","element")
 lazy val cex_highLow	= property("cex","highLow")
 lazy val cex_increment	= property("cex","increment")
 lazy val cex_index		= property("cex","index")
 lazy val cex_indicator = property("cex","indicator")
 lazy val cex_method	= property("cex","method")
 lazy val cex_slice		= property("cex","slice")
 lazy val cex_value		= property("cex","value")
 lazy val cex_weight	= property("cex","weight")
 lazy val cex_weightSchema	= property("cex","weightSchema")   
 
 lazy val dbpedia_Year 			= property("dbpedia","Year")
 
 lazy val qb_DataSet 				= resource("qb","DataSet")
 lazy val qb_DataStructureDefinition = resource("qb","DataStructureDefinition")
 lazy val qb_Slice					= resource("qb","Slice")
 lazy val qb_SliceKey				= resource("qb","SliceKey")
 lazy val qb_Observation			= resource("qb","Observation")

 lazy val qb_attribute 				= property("qb","attribute")
 lazy val qb_dataSet	 			= property("qb","dataSet")
 lazy val qb_component 			 	= property("qb","component")
 lazy val qb_componentRequired		= property("qb","componentRequired")
 lazy val qb_componentAttachment	= property("qb","componentAttachment")
 lazy val qb_componentProperty		= property("qb","componentProperty")
 lazy val qb_dimension			 	= property("qb","dimension")
 lazy val qb_measure 				= property("qb","measure")
 lazy val qb_order				 	= property("qb","order")
 lazy val qb_observation		 	= property("qb","observation")
 lazy val qb_slice					= property("qb","slice")
 lazy val qb_sliceKey				= property("qb","sliceKey")
 lazy val qb_sliceStructure		= property("qb","sliceStructure")
 lazy val qb_structure				= property("qb","structure")

 lazy val sdmxAttribute_unitMeasure = property("sdmxAttribute","unitMeasure")

 lazy val wf_onto_DSD 			= resource("wf-onto","DSD")
 lazy val wf_onto_Country 			= resource("wf-onto","Country")
 lazy val wf_onto_SecondaryIndicator = resource("wf-onto","SecondaryIndicator")
 lazy val wf_onto_PrimaryIndicator = resource("wf-onto","SecondaryIndicator")

 lazy val wf_onto_ref_year 		= property("wf-onto","ref-year")
 lazy val wf_onto_sliceByArea 	= property("wf-onto","sliceByArea")
 lazy val wf_onto_ref_area 		= property("wf-onto","ref-area")

 def property(prefix: String, name: String) : Property = {
   if (cexMap.contains(prefix)) ResourceFactory.createProperty(cexMap(prefix)+name)
   else throw new Exception("property: Prefix " + prefix + " does not belong to Prefix Map")
 }
 
 def resource(prefix: String, name: String) : Resource = {
   if (cexMap.contains(prefix)) ResourceFactory.createResource(cexMap(prefix)+name)
   else throw new Exception("resource: Prefix " + prefix + " does not belong to Prefix Map")
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