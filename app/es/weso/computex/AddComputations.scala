package es.weso.computex

import org.rogach.scallop.Scallop
import com.typesafe.config.ConfigFactory
import org.rogach.scallop.ScallopConf
import org.rogach.scallop.exceptions.Help
import org.slf4j.LoggerFactory
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.util.FileManager
import com.hp.hpl.jena.rdf.model.Model
import scala.io.Source
import es.weso.utils.JenaUtils._
import com.hp.hpl.jena.query.ResultSet
import scala.collection.mutable.ArrayBuffer
import play.api.libs.json._
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.FileOutputStream
import com.hp.hpl.jena.rdf.model.SimpleSelector
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.rdf.model.Property
import PREFIXES._
import scala.collection.JavaConverters
import es.weso.computex.profile.Profile
import es.weso.utils.StatsUtils._

class AddComputations(arguments: Array[String],
    onError: (Throwable, Scallop) => Nothing
    ) extends ScallopConf(arguments) {

    banner("""| Generate Computations
              | Options:
              |""".stripMargin)
    footer("Enjoy!")
    version("0.1")
    val fileName = opt[String]("file",
                    required=true,
        			descr = "Turtle file")
    val output  = opt[String]("out",
    				descr = "Output file")
    val version = opt[Boolean]("version", 
    				noshort = true, 
    				descr = "Print version")
    val help 	= opt[Boolean]("help", 
    				noshort = true, 
    				descr = "Show this message")
  
  override protected def onError(e: Throwable) = onError(e, builder)
}


object AddComputations extends App {
  
 def hasComputationType(m:Model, r: Resource, t: Resource) : Boolean = {
   if (hasProperty(m,r,cex_computation)) {
    val c = findProperty_asResource(m,r,cex_computation)
    val cType = findProperty_asResource(m,c,rdf_type)
    cType.asResource == t
   } else false
   
 }

 def getValue(m:Model,obs:Resource) : Double = {
   if (hasProperty(m,obs,cex_value)) {
     findProperty(m,obs,cex_value).asLiteral.getDouble
   } else {
     // logger.error("Observation " + obs + " has no value. Assumed 0")
     0.0
   }
 }

  def findDatasetWithComputation(m:Model, compType : Resource) : 
	  		Option[(Resource,Resource)] = {
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   while (iterDatasets.hasNext) {
     val dataset = iterDatasets.next
     if (hasComputationType(m,dataset,compType)) {
       return Some(dataset,findProperty_asResource(m,dataset,cex_computation))       
     }
   }
   None
 }

 def addNormalize(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   while (iterDatasets.hasNext) {
     val datasetNormalized = iterDatasets.nextResource()
     if (hasComputationType(m,datasetNormalized,cex_NormalizeDataSet)) {

       logger.info("Normalize Dataset: " + datasetNormalized)
       val computation = findProperty_asResource(m, datasetNormalized, cex_computation) 
       val dataset = findProperty_asResource(m,computation,cex_dataSet)

       // Iterate for all slices to copy
       val iterSlices = m.listObjectsOfProperty(datasetNormalized,qb_slice)
       while (iterSlices.hasNext) {
         val sliceToCopy = iterSlices.nextNode.asResource
         val year = findProperty(m,sliceToCopy,wf_onto_ref_year)
         val iterSlices2 = m.listObjectsOfProperty(dataset,qb_slice)
         while (iterSlices2.hasNext) {
           val slice = iterSlices2.nextNode.asResource
           val yearSlice = findProperty(m,slice,wf_onto_ref_year)
           if (yearSlice == year) {
             val indicator = findProperty_asResource(m,slice,cex_indicator)
             val highLow = findProperty_asResource(m,indicator,cex_highLow)
             val isHigh = highLow == cex_High
             
             // Calculate Mean and SD
             val iterObsMean = m.listObjectsOfProperty(slice,qb_observation)
             val builder = Seq.newBuilder[Double]
             while (iterObsMean.hasNext) {
               val obs = iterObsMean.nextNode.asResource
               val value = getValue(m,obs)
               builder += value
             }
             val seqObs = builder.result
             val (mean,sd) = calculateMeanSD(seqObs)
             
             val iterObs = m.listObjectsOfProperty(slice,qb_observation)
             while (iterObs.hasNext) {
               val obs = iterObs.nextNode.asResource
               val value = getValue(m,obs)
               val area = findProperty(m,obs,wf_onto_ref_area)
               val diff = if (isHigh) value - mean else mean - value 
               val zScore = diff / sd
               
               val newObs = newModel.createResource
               newModel.add(sliceToCopy,qb_observation,newObs)
               newModel.add(newObs,rdf_type,qb_Observation)
               newModel.add(newObs,cex_value,literalDouble(zScore))
               newModel.add(newObs,cex_indicator,indicator)
               newModel.add(newObs,qb_dataSet,datasetNormalized)
               newModel.add(newObs,wf_onto_ref_area,area)
               newModel.add(newObs,wf_onto_ref_year,year)
               newModel.add(newObs,sdmxConcept_obsStatus,cex_Normalized)
               val newComp = newModel.createResource
               newModel.add(newObs,cex_computation,newComp)
               newModel.add(newComp,rdf_type,cex_Normalize)
               newModel.add(newComp,cex_observation,obs)
               newModel.add(newComp,cex_slice,slice)
             }
           }
         }
       }
    }
   }
   newModel
 }

 // Todo: Unfinnished (Check Cluster.sparql query)
 def addCluster(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   while (iterDatasets.hasNext) {
     val datasetToCopy = iterDatasets.nextResource()
     if (hasComputationType(m,datasetToCopy,cex_ClusterDataSets)) {
      val computation = findProperty_asResource(m, datasetToCopy, cex_computation)
      val datasetFrom = findProperty_asResource(m, computation, cex_dataSet)
      val dim = findProperty_asResource(m,computation,cex_dimension)
      val valueDim = findProperty_asResource(m,computation,cex_value)
      
      val iterSlices = m.listObjectsOfProperty(datasetToCopy,qb_slice)
      while (iterSlices.hasNext) {
        val sliceToCopy = iterSlices.nextNode()
      }
    }
   }
   newModel
 }

 def getObsValue(m: Model, obs: Resource) : Double = {
   val value = findProperty(m,obs,cex_value)
   value.asLiteral.getDouble()
 }
 
 def getObsArea(m: Model, obs: Resource) : Resource = {
   findProperty_asResource(m,obs,wf_onto_ref_area)
 }

 def groupClusters(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   findDatasetWithComputation(m,cex_GroupClusters) match {
     case None => newModel
     case Some((dataset,computation)) => {
       println("Dataset to group: " + dataset)
       val datasetFrom = findProperty_asResource(m, computation, cex_dataSet)

       println("Dataset From: " + datasetFrom)
       val dimension	  = findProperty_asResource(m, computation, cex_dimension)
       
       val table = TableComputations.empty
       
       // Collect observations into a table
       val iterSlices = m.listObjectsOfProperty(datasetFrom,qb_slice)
       while (iterSlices.hasNext) {
        val slice = iterSlices.next.asResource
        val indicator = findProperty_asResource(m,slice,cex_indicator)
        val iterObs = m.listObjectsOfProperty(slice,qb_observation)
        while (iterObs.hasNext) {
          val obs = iterObs.next.asResource()
          val value = getObsValue(m,obs)
          val area = getObsArea(m,obs)
          table.insert(indicator,area,value)
        }
       } 
        
       table.showInfo
       
       // Fill groupings
       val iterComponents = m.listSubjectsWithProperty(rdf_type,cex_Component)
       val groupingsBuilder = Map.newBuilder[Resource,Set[Resource]]
       while (iterComponents.hasNext) {
          val component = iterComponents.nextResource()
            val iterElements = m.listObjectsOfProperty(component,cex_element)
            val elemsBuilder = Set.newBuilder[Resource]
        	while(iterElements.hasNext) {
              val element = iterElements.next.asResource
              elemsBuilder += element
            }
           groupingsBuilder+= ((component,elemsBuilder.result))
       }
       val groupings = groupingsBuilder.result
        
       // Fill weights
       val indicatorWeights = m.getResource(wi_weightSchema_indicatorWeights.getURI)
       val weightsBuilder = Map.newBuilder[Resource,Double]
       val iterWeights = m.listObjectsOfProperty(indicatorWeights,cex_weight)
       while (iterWeights.hasNext) {
          val weightNode = iterWeights.nextNode().asResource()
          val element = findProperty_asResource(m,weightNode,cex_element)
          val weight  = getValue(m,weightNode)
          weightsBuilder += ((element,weight))
       }
       
       val weights = weightsBuilder.result
        
       val newTable = table.group(groupings,weights)

       val iterSlicesTo = m.listObjectsOfProperty(dataset,qb_slice)
       while (iterSlicesTo.hasNext) {
         val sliceTo = iterSlicesTo.next.asResource
         val indicator = findProperty_asResource(m,sliceTo,cex_indicator)
         val year = findProperty(m,sliceTo,wf_onto_ref_year)
         for (area <- table.getAreas) {
           if (table.contains(indicator,area)) {
        	 val obs = newModel.createResource
             newModel.add(obs,rdf_type,qb_Observation)
             newModel.add(obs,cex_indicator,indicator)
             newModel.add(obs,wf_onto_ref_area,area)
             newModel.add(obs,wf_onto_ref_year,year)
             newModel.add(obs,cex_value,literalDouble(table.lookup(indicator,area).get))
             newModel.add(sliceTo,qb_observation,obs)
             val comp = newModel.createResource()
             newModel.add(obs,cex_computation,comp)
             newModel.add(comp,rdf_type,cex_GroupMean)
             newModel.add(comp,cex_weightSchema,indicatorWeights)
           }
         }
       }
        
       newModel
     }
   }
 }
 
 def groupSubIndex(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   findDatasetWithComputation(m,cex_GroupSubIndex) match {
     case None => newModel
     case Some((dataset,computation)) => {
       println("Dataset to group: " + dataset)
       val datasetFrom = findProperty_asResource(m, computation, cex_dataSet)

       println("Dataset From: " + datasetFrom)
       val dimension	  = findProperty_asResource(m, computation, cex_dimension)
       
       val table = TableComputations.empty
       
       // Collect observations into a table
       val iterSlices = m.listObjectsOfProperty(datasetFrom,qb_slice)
       while (iterSlices.hasNext) {
        val slice = iterSlices.next.asResource
        val indicator = findProperty_asResource(m,slice,cex_indicator)
        val iterObs = m.listObjectsOfProperty(slice,qb_observation)
        while (iterObs.hasNext) {
          val obs = iterObs.next.asResource()
          println("Obs: " + obs)
          val value = getObsValue(m,obs)
          val area = getObsArea(m,obs)
          table.insert(indicator,area,value)
        }
       } 
        
       table.showInfo

       // Fill groupings
       val iterSubindexes = m.listSubjectsWithProperty(rdf_type,cex_SubIndex)
       val groupingsBuilder = Map.newBuilder[Resource,Set[Resource]]
       while (iterSubindexes.hasNext) {
         val component = iterSubindexes.nextResource()
         val iterElements = m.listObjectsOfProperty(component,cex_element)
         val elemsBuilder = Set.newBuilder[Resource]
         while(iterElements.hasNext) {
            val element = iterElements.next.asResource
              elemsBuilder += element
         }
         groupingsBuilder+= ((component,elemsBuilder.result))
       }
       val groupings = groupingsBuilder.result
        
        // Fill weights
        val componentWeights = m.getResource(wi_weightSchema_componentWeights.getURI)
        val weightsBuilder = Map.newBuilder[Resource,Double]
        val iterWeights = m.listObjectsOfProperty(componentWeights,cex_weight)
        while (iterWeights.hasNext) {
          val weightNode = iterWeights.nextNode().asResource()
          val element = findProperty_asResource(m,weightNode,cex_element)
          val weight  = getValue(m,weightNode)
          weightsBuilder += ((element,weight))
        }
        val weights = weightsBuilder.result
        
        val newTable = table.group(groupings,weights)

        val iterSlicesTo = m.listObjectsOfProperty(dataset,qb_slice)
        while (iterSlicesTo.hasNext) {
         val sliceTo = iterSlicesTo.next.asResource
         val indicator = findProperty_asResource(m,sliceTo,cex_indicator)
         val year = findProperty(m,sliceTo,wf_onto_ref_year)
         for (area <- table.getAreas) {
           if (table.contains(indicator,area)) {
        	 val obs = newModel.createResource
             newModel.add(obs,rdf_type,qb_Observation)
             newModel.add(obs,cex_indicator,indicator)
             newModel.add(obs,wf_onto_ref_area,area)
             newModel.add(obs,wf_onto_ref_year,year)
             newModel.add(obs,cex_value,literalDouble(table.lookup(indicator,area).get))
             newModel.add(sliceTo,qb_observation,obs)
             val comp = newModel.createResource()
             newModel.add(obs,cex_computation,comp)
             newModel.add(comp,rdf_type,cex_GroupMean)
             newModel.add(comp,cex_weightSchema,componentWeights)
           }
         }
        }
        
        newModel

     }
   }
 }

 def groupIndex(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   findDatasetWithComputation(m,cex_GroupIndex) match {
     case None => newModel
     case Some((dataset,computation)) => {
       println("Dataset to group: " + dataset)
       val datasetFrom = findProperty_asResource(m, computation, cex_dataSet)

       println("Dataset From: " + datasetFrom)
       val dimension	  = findProperty_asResource(m, computation, cex_dimension)
       
       val table = TableComputations.empty
       
       // Collect observations into a table
       val iterSlices = m.listObjectsOfProperty(datasetFrom,qb_slice)
       while (iterSlices.hasNext) {
        val slice = iterSlices.next.asResource
        val indicator = findProperty_asResource(m,slice,cex_indicator)
        val iterObs = m.listObjectsOfProperty(slice,qb_observation)
        while (iterObs.hasNext) {
          val obs = iterObs.next.asResource()
          val value = getObsValue(m,obs)
          val area = getObsArea(m,obs)
          table.insert(indicator,area,value)
        }
       } 
        
       table.showInfo

       // Fill groupings
       val index = m.getResource(wi_index_index.getURI)
       val groupingsBuilder = Map.newBuilder[Resource,Set[Resource]]
       val iterElements = m.listObjectsOfProperty(index,cex_element)
       val elemsBuilder = Set.newBuilder[Resource]
       while(iterElements.hasNext) {
         val element = iterElements.next.asResource
         elemsBuilder += element
       }
       groupingsBuilder+= ((index,elemsBuilder.result))
       val groupings = groupingsBuilder.result
        
       // Fill weights
       val subindexWeights = m.getResource(wi_weightSchema_subindexWeights.getURI)
       val weightsBuilder = Map.newBuilder[Resource,Double]
       val iterWeights = m.listObjectsOfProperty(subindexWeights,cex_weight)
       while (iterWeights.hasNext) {
          val weightNode = iterWeights.nextNode().asResource()
          val element = findProperty_asResource(m,weightNode,cex_element)
          val weight  = getValue(m,weightNode)
          weightsBuilder += ((element,weight))
        }
       val weights = weightsBuilder.result
        
       val newTable = table.group(groupings,weights)

       val iterSlicesTo = m.listObjectsOfProperty(dataset,qb_slice)
       while (iterSlicesTo.hasNext) {
         val sliceTo = iterSlicesTo.next.asResource
         val indicator = findProperty_asResource(m,sliceTo,cex_indicator)
         val year = findProperty(m,sliceTo,wf_onto_ref_year)
         for (area <- table.getAreas) {
           if (table.contains(indicator,area)) {
        	 val obs = newModel.createResource
             newModel.add(obs,rdf_type,qb_Observation)
             newModel.add(obs,cex_indicator,indicator)
             newModel.add(obs,wf_onto_ref_area,area)
             newModel.add(obs,wf_onto_ref_year,year)
             newModel.add(obs,cex_value,literalDouble(table.lookup(indicator,area).get))
             newModel.add(sliceTo,qb_observation,obs)
             val comp = newModel.createResource()
             newModel.add(obs,cex_computation,comp)
             newModel.add(comp,rdf_type,cex_GroupMean)
             newModel.add(comp,cex_weightSchema,subindexWeights)
           }
         }
        }
        
        newModel

     }
   }
 }

 def addComputations(m: Model) : Model = {
   AddDatasets.addDatasets(m)
   val normalize = addNormalize(m)
   println("Normalized: " + normalize.size)
   m.add(normalize)  

   val cex = Profile.Computex
   cex.expandStep("Cluster", m)
   cex.expandStep("WeightedSimple",m) 
   val groups = groupClusters(m)
   println("Groups: " + groups.size)
   m.add(groups) 

   val subindexes = groupSubIndex(m)
   println("Subindexes: " + subindexes.size)
   m.add(subindexes) 

   val index = groupIndex(m)
   println("Index: " + index.size)
   m.add(index) 

//   AddRanking.addRankings(m)
   m
 } 

 override def main(args: Array[String]) {

  val logger 		= LoggerFactory.getLogger("Application")
  
  val conf 			= ConfigFactory.load()
  
  val opts 	= new AddDatasetsOpts(args,onError)
  try {
   val model = ModelFactory.createDefaultModel
   val inputStream = FileManager.get.open(opts.fileName())
   model.read(inputStream,"","TURTLE")
   val newModel = addComputations(model)
   if (opts.output.get == None) newModel.write(System.out,"TURTLE")
   else {
     val fileOutput = opts.output()
     newModel.write(new FileOutputStream(fileOutput),"TURTLE")
   }
  } catch {
    case e: Exception => println("\nException:\n" + e.getLocalizedMessage())
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
