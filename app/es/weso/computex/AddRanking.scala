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

class AddRanking(arguments: Array[String],
    onError: (Throwable, Scallop) => Nothing
    ) extends ScallopConf(arguments) {

    banner("""| Add Rankings
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

object AddRanking extends App {
 
 def rankings(m:Model) : Model = {
   val newModel = ModelFactory.createDefaultModel()
   
   // we suppose dim is wf_onto_ref_area
   val dim = wf_onto_ref_area // findProperty_asResource(m,comp,cex_dimension)
         
   val compsRankingIter = m.listSubjectsWithProperty(rdf_type,cex_RankingDataset)
   while (compsRankingIter.hasNext) {
     val comp = compsRankingIter.nextResource()
     println("Ranking computation " + comp)
     val datasetToCopy = findSubject_asResource(m,comp,cex_computation)
     println("Dataset to copy: " + datasetToCopy)

     val iterSlicesToCopy = m.listObjectsOfProperty(datasetToCopy,qb_slice)
     while (iterSlicesToCopy.hasNext) {
       val sliceToCopy = iterSlicesToCopy.nextNode().asResource
       val indicatorToCopy = findProperty(m,sliceToCopy,cex_indicator)
       println("Slice to copy: " + sliceToCopy)

       val iterSlicesToRank = m.listObjectsOfProperty(comp,cex_slice)
       while (iterSlicesToRank.hasNext) {
         val sliceToRank = iterSlicesToRank.nextNode().asResource
         val indicatorToRank = findProperty(m,sliceToRank,cex_indicator)
         if (indicatorToCopy == indicatorToRank) {
        	 println("Slice to rank: " + sliceToRank + " with indicator " + indicatorToCopy.asResource.getLocalName)
        	 
         // Collect all observations to rank
         val iter1 = m.listObjectsOfProperty(sliceToRank, qb_observation)
         val builder = Seq.newBuilder[(Resource,Double)]
         while (iter1.hasNext) {
          val obsToRank = iter1.next().asResource()
          val valueToRank = findProperty_asLiteral(m,obsToRank,cex_value).getDouble
          builder += ((obsToRank,valueToRank))
         }
     
         // Sort observations to rank
         val sorted = scala.util.Sorting.stableSort(builder.result,
          (p1: (Resource,Double),p2 : (Resource,Double)) => p1._2 < p2._2)

          // Loop to create rankings
          val iter2 = m.listObjectsOfProperty(sliceToRank, qb_observation)
          while (iter2.hasNext) {
        	  val obsToRank = iter2.next().asResource()
        	  val valueDim = findProperty(m,obsToRank,dim)
              val valueYear = findProperty(m,obsToRank,wf_onto_ref_year)
              val valueToRank = findProperty(m,obsToRank,cex_value)
              val ranking = sorted.indexWhere(p => p._1 == obsToRank) + 1 
              println("Ranking of " + obsToRank + " with value " + valueToRank + "= " + ranking) 
      
        	  // Construct observation 
        	  val obs = m.createResource()
        	  m.add(sliceToCopy,qb_observation,obs)
        	  m.add(obs,rdf_type,qb_Observation)
        	  m.add(obs,qb_dataSet,datasetToCopy)
        	  m.add(obs,wf_onto_ref_year,valueYear)
        	  m.add(obs,dim,valueDim)
        	  m.add(obs,cex_value,literalInt(ranking))

        	  val comp = m.createResource()
        	  m.add(obs,cex_computation,comp)
        	  m.add(comp,rdf_type,cex_Ranking)
        	  m.add(comp,cex_reason,"Ranking of observation in Slice")
        	  m.add(comp,cex_slice,sliceToRank)
        	  m.add(comp,cex_dimension,dim)
        	  m.add(comp,cex_observation,obsToRank)
          }
         }
       }
     }
   }
   newModel.setNsPrefixes(PREFIXES.cexMapping)
   newModel
 }

 def addRankings(m: Model) : Model = {
   m.add(rankings(m))
 } 

  override def main(args: Array[String]) {
  val logger 		= LoggerFactory.getLogger("Application")
  val conf 			= ConfigFactory.load()
  
  val opts 	= new AddDatasetsOpts(args,onError)
  try {
   val model = ModelFactory.createDefaultModel
   val inputStream = FileManager.get.open(opts.fileName())
   model.read(inputStream,"","TURTLE")
   val newModel = addRankings(model)
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
