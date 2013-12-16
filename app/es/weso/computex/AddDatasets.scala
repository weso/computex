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

 lazy val YearDataset = 2013
 
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
     if (hasComputation(m,dataset,cex_Imputed) || hasComputation(m,dataset,cex_Raw)) {
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

 def clusterIndicatorsDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = wi_dataset_ClusterIndicators

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_ClusterDataSets)
   newModel.add(newDataSet,cex_computation,computation)
   // Collect normalized datasets
   val iterDatasets = m.listSubjectsWithProperty(rdf_type,qb_DataSet)
   while (iterDatasets.hasNext) {
     val dataset = iterDatasets.nextResource
     if (hasComputation(m,dataset,cex_NormalizeDataSet)) {
       newModel.add(computation,cex_dataSet,dataset)
     }
   }
   val dim = wf_onto_ref_year
   val valueDim = literalInt(YearDataset)
   newModel.add(computation,cex_dimension,dim)
   // TODO: Year should be a parameter
   newModel.add(computation,cex_value,valueDim)

   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)
   
   // Todo: Create slices for each indicator
   val iterIndicators = m.listSubjectsWithProperty(rdf_type,cex_Indicator)
   while (iterIndicators.hasNext) {
     val indicator = iterIndicators.nextResource
     val sliceIndicator = newModel.createResource
     newModel.add(sliceIndicator,rdf_type,qb_Slice)
     newModel.add(newDataSet,qb_slice,sliceIndicator)
     newModel.add(sliceIndicator,cex_indicator,indicator)
     newModel.add(sliceIndicator,dim,valueDim)
   }
   
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

/* def indicatorsWeightedDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = newModel.createResource(wi_dataset_IndicatorsWeighted.getURI)

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_WeightedSimple)
   newModel.add(computation,cex_dataSet,wi_dataset_ClusterIndicators)
   newModel.add(computation,cex_weightSchema,wi_weightSchema_indicatorWeights)
   newModel.add(newDataSet,cex_computation,computation)
   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)
   
   val iterIndicators = m.listSubjectsWithProperty(rdf_type,cex_Indicator)
   while (iterIndicators.hasNext) {
     val indicator = iterIndicators.nextResource
     val slice = newModel.createResource()
     newModel.add(slice,rdf_type,qb_Slice)
     newModel.add(slice,cex_indicator,indicator)
     newModel.add(slice,wf_onto_ref_year,literalInt(YearDataset))
     newModel.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
     newModel.add(newDataSet,qb_slice,slice)
   }
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 } */

  def clustersGroupedDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = wi_dataset_ClustersGrouped // newModel.createResource(wi_dataset_ClustersGrouped.getURI)

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_GroupClusters)
   newModel.add(computation,cex_dataSet,wi_dataset_ClusterIndicators)
   newModel.add(computation,cex_dimension,wf_onto_ref_area)

   
   newModel.add(newDataSet,cex_computation,computation)

   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)
   
   // Collect components
   val iterComponents = m.listSubjectsWithProperty(rdf_type,cex_Component)
   while (iterComponents.hasNext) {
     val component = iterComponents.nextResource()
     newModel.add(computation,cex_component,component)
     
     val slice = newModel.createResource()
     newModel.add(slice,rdf_type,qb_Slice)
     newModel.add(slice,cex_indicator,component)
     newModel.add(slice,wf_onto_ref_year,literalInt(YearDataset))
     newModel.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
     newModel.add(newDataSet,qb_slice,slice)
   }

   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

  def mkSlice(m: Model, s: String) : Resource = m.createResource(wi_slice + s)
  def mkSlice(m:Model, r: Resource) : Resource = mkSlice(m,r.getLocalName)
  
  def mkRanking(m: Model, s: String) : Resource  = m.createResource(wi_ranking + s)
  def mkRanking(m: Model, r: Resource): Resource = mkRanking(m,r.getLocalName)

  def subindexGroupedDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = wi_dataset_SubIndexGrouped // newModel.createResource(wi_dataset_SubIndexGrouped.getURI)

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_GroupSubIndex)
   newModel.add(computation,cex_dataSet,wi_dataset_ClustersGrouped)
   newModel.add(computation,cex_dimension,wf_onto_ref_area)

   newModel.add(newDataSet,cex_computation,computation)

   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)
   
   // Collect subindexes
   val iterSubindexes = m.listSubjectsWithProperty(rdf_type,cex_SubIndex)
   while (iterSubindexes.hasNext) {
     val subindex = iterSubindexes.nextResource()
     newModel.add(computation,cex_component,subindex)
     
     val slice = mkSlice(newModel,subindex)
     newModel.add(slice,rdf_type,qb_Slice)
     newModel.add(slice,cex_indicator,subindex)
     newModel.add(slice,wf_onto_ref_year,literalInt(YearDataset))
     newModel.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
     newModel.add(newDataSet,qb_slice,slice)
   }

   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def compositeDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = wi_dataset_Composite // newModel.createResource(wi_dataset_Composite.getURI)

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_GroupIndex)
   newModel.add(computation,cex_dataSet,wi_dataset_SubIndexGrouped)
   newModel.add(computation,cex_dimension,wf_onto_ref_area)

   newModel.add(newDataSet,cex_computation,computation)

   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)
   
   val index = newModel.createResource(wi_index_index.getURI)
   newModel.add(computation,cex_component,index)
     
   val slice = mkSlice(m,"Composite")
   newModel.add(slice,rdf_type,qb_Slice)
   newModel.add(slice,cex_indicator,index)
   newModel.add(slice,wf_onto_ref_year,literalInt(YearDataset))
   newModel.add(slice,qb_sliceStructure,wf_onto_sliceByArea)
   newModel.add(newDataSet,qb_slice,slice)

   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def rankingsDataset(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   val newDataSet = wi_dataset_Rankings // newModel.createResource(wi_dataset_Rankings.getURI)

   newModel.add(newDataSet,rdf_type,qb_DataSet)
   
   val computation = newModel.createResource
   newModel.add(computation,rdf_type,cex_RankingDataset)
   newModel.add(computation,cex_dimension,wf_onto_ref_area)
   newModel.add(newDataSet,cex_computation,computation)

   newModel.add(newDataSet,sdmxAttribute_unitMeasure,dbpedia_Year)
   newModel.add(newDataSet,qb_structure,wf_onto_DSD)

   val iterSubindexes = m.listSubjectsWithProperty(rdf_type,cex_SubIndex)
   while (iterSubindexes.hasNext) {
     val subindex = iterSubindexes.nextResource
     newModel.add(computation,cex_slice,mkSlice(m,subindex))
     val sliceToRank = mkRanking(m,subindex)
     newModel.add(newDataSet,qb_slice,sliceToRank)
     newModel.add(sliceToRank,rdf_type,qb_slice)
     newModel.add(sliceToRank,cex_indicator,subindex)
     newModel.add(sliceToRank,wf_onto_ref_year,literalInt(YearDataset))
     newModel.add(sliceToRank,qb_sliceStructure,wf_onto_sliceByArea)
   }
   
   val sliceRankComposite = mkRanking(m,"Composite")
   newModel.add(computation,cex_slice,mkSlice(m,"Composite"))
   newModel.add(newDataSet,qb_slice,sliceRankComposite)
   newModel.add(sliceRankComposite,rdf_type,qb_slice)
   newModel.add(sliceRankComposite,cex_indicator,wi_index_index)
   newModel.add(sliceRankComposite,wf_onto_ref_year,literalInt(YearDataset))
   newModel.add(sliceRankComposite,qb_sliceStructure,wf_onto_sliceByArea)

   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def addDatasets(m: Model) : Model = {
//   m.add(imputedDatasets(m))
   m.add(normalizedDatasets(m))
   m.add(clusterIndicatorsDataset(m))
//   m.add(indicatorsWeightedDataset(m))
   m.add(clustersGroupedDataset(m))
   m.add(subindexGroupedDataset(m))
   m.add(compositeDataset(m))
   m.add(rankingsDataset(m))
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
