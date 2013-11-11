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

class AddDatasetsOpts(arguments: Array[String],
    onError: (Throwable, Scallop) => Nothing
    ) extends ScallopConf(arguments) {

    banner("""| Generate Computation Datasets
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

object AddDatasets extends App {

 def hasComputation(m:Model, r:Resource, t:Resource) : Boolean = {
   if (hasProperty(m,r,cex_computation)) {
     val comp = findProperty_asResource(m,r,cex_computation)
     val typeComp = findProperty_asResource(m,comp,rdf_type)
     typeComp == t
   } else
      false
 }
  
 def imputedDatasets(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val iter = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   
   while (iter.hasNext) {
     val dataset = iter.nextResource()
     val newDataSet = newModel.createResource()
     newModel.add(newDataSet,rdf_type,qb_DataSet)
     
     val computation = newModel.createResource
     newModel.add(computation,rdf_type,cex_ImputeDataSet)
     newModel.add(computation,cex_method,cex_AvgGrowth2Missing)
     newModel.add(computation,cex_method,cex_MeanBetweenMissing)
     newModel.add(computation,cex_method,cex_CopyRaw)
     newModel.add(computation,cex_dataSet,dataset)
     
     newModel.add(newDataSet,cex_computation,computation)
     newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
     newModel.add(newDataSet,qb_structure,wf_onto_DSD)

     val iterSlices = m.listStatements(dataset,qb_slice,null : RDFNode)
     while (iterSlices.hasNext) {
       val slice = iterSlices.next.getObject().asResource()
       val newSlice = newModel.createResource()
       newModel.add(newSlice,rdf_type,qb_Slice)
       newModel.add(newSlice,cex_indicator,findProperty_asResource(m,slice,cex_indicator))
       newModel.add(newSlice,wf_onto_ref_year,findProperty_asLiteral(m,slice,wf_onto_ref_year))
       newModel.add(newSlice,qb_sliceStructure,wf_onto_sliceByArea)
       newModel.add(newDataSet,qb_slice,newSlice)
     }
   }
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }
 
 def normalizedDatasets(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val datasetsIter = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   
   while (datasetsIter.hasNext) {
     val dataset = datasetsIter.nextResource()
     val localName = dataset.getLocalName()
//     val computation = findProperty_asResource(m,dataset,cex_computation)
//     val typeComputation = findProperty(m,computation,rdf_type)
//     if (typeComputation == cex_ImputeDataSet) {
     if (localName.contains("Imputed")) {
       val newDataSet = newModel.createResource()
       newModel.add(newDataSet,rdf_type,qb_DataSet)
       
       val computation = newModel.createResource
       newModel.add(computation,rdf_type,cex_NormalizeDataSet)
       newModel.add(computation,cex_dataSet,dataset)
       newModel.add(newDataSet,cex_computation,computation)
       newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
       newModel.add(newDataSet,qb_structure,wf_onto_DSD)

       val iterSlices = m.listStatements(dataset,qb_slice,null : RDFNode)
       while (iterSlices.hasNext) {
         val slice = iterSlices.next.getObject().asResource()
         val newSlice = newModel.createResource()
         newModel.add(newSlice,rdf_type,qb_Slice)
         newModel.add(newSlice,cex_indicator,findProperty_asResource(m,slice,cex_indicator))
         newModel.add(newSlice,wf_onto_ref_year,findProperty_asLiteral(m,slice,wf_onto_ref_year))
         newModel.add(newSlice,qb_sliceStructure,wf_onto_sliceByArea)
         newModel.add(newDataSet,qb_slice,newSlice)
       }
     }
   }
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def adjustedDatasets(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val datasetsIter = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   
   while (datasetsIter.hasNext) {
     val dataset = datasetsIter.nextResource()
     val computation = findProperty_asResource(m,dataset,cex_computation)
     val typeComputation = findProperty(m,computation,rdf_type)
     if (typeComputation == cex_NormalizeDataSet) {
       val newDataSet = newModel.createResource()
       newModel.add(newDataSet,rdf_type,qb_DataSet)
       
       val computation = newModel.createResource
       newModel.add(computation,rdf_type,cex_AdjustDataSet)
       newModel.add(computation,cex_dataSet,dataset)
       newModel.add(newDataSet,cex_computation,computation)
       newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
       newModel.add(newDataSet,qb_structure,wf_onto_DSD)

       val iterSlices = m.listStatements(dataset,qb_slice,null : RDFNode)
       while (iterSlices.hasNext) {
         val slice = iterSlices.next.getObject().asResource()
         val newSlice = newModel.createResource()
         newModel.add(newSlice,rdf_type,qb_Slice)
         newModel.add(newSlice,cex_indicator,findProperty_asResource(m,slice,cex_indicator))
         newModel.add(newSlice,wf_onto_ref_year,findProperty_asLiteral(m,slice,wf_onto_ref_year))
         newModel.add(newSlice,qb_sliceStructure,wf_onto_sliceByArea)
         newModel.add(newDataSet,qb_slice,newSlice)
       }
     }
   }
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def clusterDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = newModel.createResource(wi_dataset_ClusterIndicators)

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_ClusterDataSets)

   // Collect normalized datasets
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   while (iterDatasets.hasNext) {
     val dataset = iterDatasets.nextResource
     if (hasComputation(m,dataset,cex_NormalizeDataSet)) {
       newModel.add(computation,cex_dataSet,dataset)
     }
   }
   newModel.add(computation,cex_dimension,wf_onto_ref_year)
   // TODO: Year should be a parameter
   newModel.add(computation,cex_value,literalInt(2013))

   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)
   // Todo
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def indicatorsWeightedDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = newModel.createResource(wi_dataset_IndicatorsWeighted)

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_WeightedSimple)
   newModel.add(computation,cex_dataSet,wi_dataset_ClusterIndicators)
   newModel.add(newDataSet,cex_computation,computation)
   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)
   // Todo
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def addDatasets(m: Model) : Model = {
//   m.add(imputedDatasets(m))
   m.add(normalizedDatasets(m))
 } 

 override def main(args: Array[String]) {

  val logger 		= LoggerFactory.getLogger("Application")
  val conf 			= ConfigFactory.load()
  
  val opts 	= new AddDatasetsOpts(args,onError)
  try {
   val model = ModelFactory.createDefaultModel
   val inputStream = FileManager.get.open(opts.fileName())
   model.read(inputStream,"","TURTLE")
   val newModel = addDatasets(model)
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
