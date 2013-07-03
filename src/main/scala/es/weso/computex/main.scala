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

object Main extends App {

  val opts = new ScallopConf(args) {
    banner("""Computex. Compute and validate Statistical Index Observations
              Options: --indexURI <uri> 
              For usage see below:
           """.trim())
    val indexURI = opt[String]("indexURI")
    val version = opt[Boolean]("version", noshort = true, descr = "Print version")
    val help = opt[Boolean]("help", noshort = true, descr = "Show this message")
  }

  val logger = LoggerFactory.getLogger("Application")

  val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val closureFile  = conf.getString("closureFile")
  val flattenFile  = conf.getString("flattenFile")
  val indexDataURI = opts.indexURI.get.getOrElse(conf.getString("indexDataURI")) 

  val cex = new Computex
  cex.computex(indexDataURI,ontologyURI,validationDir,computationDir)

}