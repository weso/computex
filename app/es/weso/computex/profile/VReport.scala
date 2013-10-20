package es.weso.computex.profile

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asScalaIterator
import scala.io.Source.fromFile
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.DatasetFactory
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.update.GraphStore
import com.hp.hpl.jena.update.GraphStoreFactory
import com.hp.hpl.jena.update.UpdateAction
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CIntegrityQuery
import es.weso.utils.JenaUtils._
import play.api.Logger
import es.weso.computex.entities.CQuery
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.io.OutputStream
import scala.util.Random
import java.io.StringWriter
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.rdf.model.Literal
import java.io.FileOutputStream

package object VReport {

  type VReport = ValidationReport[
     Seq[Validator],  				 		 // List of validators that passed
    (Seq[Validator],Seq[(Validator,Model)])  // Pair with list of validators that passed and 
    								 		 // list of (validators,error models) that didn't pass
  ]
  
  
 def show(
     vr: VReport, 
     verbose: Boolean = false) : String = {
   vr match {
     case Passed(vs) => 
      if (verbose) {
       "Passed \nValidators: " + vs
      } else {
       "Passed " + vs.length + " validators"
      }
     case NotPassed((vs,nvs)) =>
       if (verbose) {
         "Not passed\n" + 
         "Validators that passed: " + vs +
         "Validators that didn't pass: " + nvs
       } else {
         "Not passed:\n" + 
         showNotPassed(nvs) + 
         "Passed: " + vs.length + " validators"
       }
   }
 }
  
  def showNotPassed(vals : Seq[(Validator,Model)]) = {
    vals.foldLeft("")((r,p) => p._1.name + ". " + model2Str(p._2) + r)
  }
}
