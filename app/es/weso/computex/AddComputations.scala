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

 def hasComputationType(m:Model, c: Resource, t: Resource) : Boolean = {
   val cType = findProperty_asResource(m,c,rdf_type)
   cType.asResource == t
 }

 def getValue(m:Model,obs:Resource) : Double = {
   if (hasProperty(m,obs,cex_value)) {
     findProperty(m,obs,cex_value).asLiteral.getDouble
   } else {
     logger.error("Observation " + obs + " has no value. Assumed 0")
     0.0
   }
 }
 
 def addNormalize(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   while (iterDatasets.hasNext) {
     val datasetNormalized = iterDatasets.nextResource()
     if (hasProperty(m,datasetNormalized,cex_computation)) {
      val computation = findProperty_asResource(m, datasetNormalized, cex_computation)
      if (hasComputationType(m,computation,cex_NormalizeDataSet)) {
       logger.info("Normalize Dataset: " + datasetNormalized)
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
               val diff = 
                 	if (isHigh) value - mean 
                    else mean - value 
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
   }
   newModel
 }
 
 def addComputations(m: Model) : Model = {
   AddDatasets.addDatasets(m)
   // val cex = Profile.Computex
   // cex.expandStep("zScores",m)
   m.add(addNormalize(m))  
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
