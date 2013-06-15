package es.weso.validex

import scala.collection.JavaConversions._
import java.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.ontology.OntModelSpec._


object Application extends App {
  
  val validationDirName = "../ontology/validation"

    try {
     println("Validex: Validating index data")
     printCWD()
     readValidationFiles();
    } catch {
      case ex : Exception => println("Exception: " + ex)
    }

def printCWD() {
  val cwd = new File(".")

  println("CWD = " + cwd.getAbsolutePath())
}
   
 def readValidationFiles() {
  val validationDir = new File(validationDirName)

  if (validationDir == null || validationDir.listFiles == null) 
     throw new IOException("Validation directory: " + validationDirName + " not accessible")
  else {
    for(file <- validationDir.listFiles if file.getName endsWith ".sparql"){
    // process the file
     println("Validation file: " + file)
    }   
   }
 }  
}