package es.weso.computex

import scala.collection.JavaConversions._
import java.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.ontology.OntModelSpec._
import org.slf4j._
import com.hp.hpl.jena.rdf.model.Model
import org.rogach.scallop._
import com.typesafe.config._
import es.weso.utils.JenaUtils
import org.rogach.scallop.exceptions.Help
import org.rogach.scallop.exceptions.ScallopException
import org.rogach.scallop.exceptions.RequiredOptionNotFound
import es.weso.computex.profile.Passed
import es.weso.computex.profile.NotPassed
import es.weso.computex.profile.Validator

class Opts(arguments: Array[String],
    onError: (Throwable, Scallop) => Nothing
    ) extends ScallopConf(arguments) {

    banner("""| Compute and/or validate RDF Data
              | Options:
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
    				descr = "Profile used to validate: [Cube, Computex, <anyURI>]")
    val expand 	= toggle("expand", 
    				prefix = "no-", 
    				default = Some(true),
    				descrYes = "expand model before validation", 
        			descrNo  = "disable model expansion before validation")
    val imports  = toggle("imports", 
    				prefix = "no-",
    				default = Some(true),
    				descrYes = "enable profile imports", 
        			descrNo = "disable profile imports")
    val showModel  = toggle("show", 
    				prefix = "no-",
    				default = Some(false),
    				descrYes = "show models ", 
        			descrNo = "don't show models")
   val verbose    = toggle("verbose", 
    				prefix = "no-",
    				default = Some(false),
    				descrYes = "Normal output", 
        			descrNo = "Verbose output")
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
		  opts.expand(),
		  opts.imports())
       val (report,model) = driver.validate
       report match {
         case Passed(vs) => println("Valid. " + vs.length + " validators passed\n")
         case NotPassed((vs,nvs)) => 
           println("Not valid: ")
           println(vs.length + " validators passed")
           println(nvs.length + " validators didn't pass")
           for ((v,m) <- nvs) {
             println("Validator failed: " + v.name)
             if (opts.verbose()) println("Model:" + JenaUtils.model2Str(m))
           }
       }

     if (opts.showModel())   {
         showModel(opts.output.get,model,"TURTLE","Validated Model")
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
  
  private def showModel(optName : Option[String], 
		 model: Model, 
		 syntax: String, 
		 msg: String) : Unit = {
  optName match {
    case None => {
      println(msg)
      JenaUtils.model2Str(model,syntax)
    }
    case Some(outFile) => {
      JenaUtils.model2File(model,outFile,syntax)
    }
  }
 }
  
 def showValidators (vals: Seq[Validator]) : String = {
   vals.foldLeft("")((r,v) => v.name + "\n" + r)
 }
} 
