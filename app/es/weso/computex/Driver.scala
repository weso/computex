package es.weso.computex

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
import es.weso.utils.JenaUtils.Turtle
import play.api.Logger
import es.weso.computex.entities.CQuery
import es.weso.utils.JenaUtils
import es.weso.computex.profile.Profile
import es.weso.computex.profile.ProfileParser
import scala.io.Source

case class Driver(
    ontologyURI: String,
    dataURI: String,
    syntax: String,
    profileName: String,
    expand: Boolean,
    imports: Boolean) {

 type VReport = ValidationReport[Model,Seq[Validator],(Seq[Validator],Seq[Validator])]
 
 def validate : (VReport,Model) = {
   val ontology = JenaUtils.parseFromURI(ontologyURI)
   val data 	= JenaUtils.parseFromURI(dataURI)
   data.add(ontology)
   val profile = profileName match {
     case "Cube" 	 => Profile.Cube
     case "Computex" => Profile.Computex
     case _ => {
    	 val contents 	= Source.fromFile(profileName).mkString
    	 val model 		= JenaUtils.parseModel(contents)
    	 ProfileParser.fromModel(model)(0)
     }
   }
   
   profile.validate(data,expand,imports)
 }
}

object Driver extends App {
}