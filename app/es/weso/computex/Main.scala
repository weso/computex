package es.weso.computex

import scala.collection.JavaConversions._
import java.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.ontology.OntModelSpec._
import org.slf4j._
import com.hp.hpl.jena.rdf.model._
import org.rogach.scallop._
import com.typesafe.config._
import es.weso.utils.JenaUtils
import org.rogach.scallop.exceptions.Help
import org.rogach.scallop.exceptions.ScallopException
import org.rogach.scallop.exceptions.RequiredOptionNotFound


class Opts(arguments: Array[String],
    onError: (Throwable, Scallop) => Nothing
    ) extends ScallopConf(arguments) {

    banner("""| Compute and/or validate RDF Data
              | Options: --data <uri>
              |          --profile <uri>
              | For usage see below:
              |""".stripMargin)
    footer("Enjoy!")
    version("Computex 0.1")
    val data 	= opt[String]("data",
    				required=true,
    				descr = "Data file")
    val syntax 	= opt[String]("syntax", 
        			default=Some("TURTLE"), 
        			descr = "Turtle, N-TRIPLES, RDF/XML")
    val profile = opt[String]("profile",
    				default=Some("Cube"),
    				descr = "Profile used to validate: Cube, Computex")
    val extend  = toggle("extend", 
    				prefix = "no-", 
    				default = Some(true),
    				descrYes = "enable extension queries", 
        			descrNo = "disable extension queries")
    val imports  = toggle("imports", 
    				prefix = "no-",
    				default = Some(true),
    				descrYes = "enable profile imports", 
        			descrNo = "disable profile imports")
    val output  = opt[String]("out",
    				descr = "Output model to file")
    val report  = opt[String]("report",
    				descr = "Output report model to file")
    val version = opt[Boolean]("version", 
    				noshort = true, 
    				descr = "Print version")
    val help 	= opt[Boolean]("help", 
    				noshort = true, 
    				descr = "Show this message")
  
  override protected def onError(e: Throwable) = onError(e, builder)
}

object Main extends App {


 override def main(args: Array[String]) {

  val logger 		= LoggerFactory.getLogger("Application")
  val conf 			= ConfigFactory.load()
  
  val opts 	= new Opts(args,onError)
  try {
       val driver = Driver(conf.getString("ontologyURI"),
		  opts.data(),
		  opts.syntax(),
		  opts.profile(),
		  opts.extend(),
		  opts.imports())
	   println("Driver: " + driver)
       val (report,model) = driver.validate
       report match {
         case Passed => println("Valid")
         case NotPassed(reportModel) => showModel(opts.report.get,reportModel,"TURTLE","Report")
       }
       showModel(opts.output.get,model,"TURTLE","Model")
     } catch {
  	    case e: Exception => println("Exception:" + e)
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
  
  private def showModel(optName : Option[String], 
		 model: Model, 
		 syntax: String, 
		 msg: String) : Unit = {
  optName match {
    case None => {
      println(msg)
      model.write(System.out,syntax)
    }
    case Some(outFile) => {
      JenaUtils.model2File(model,outFile)
    }
  }
 }
  
} 
