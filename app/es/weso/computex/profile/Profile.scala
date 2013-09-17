package es.weso.computex.profile

import com.hp.hpl.jena.rdf.model.Model
import es.weso.utils.JenaUtils._
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.net.URI
import es.weso.computex.profile._
import es.weso.utils.ConfigUtils
import scala.io.Source
import es.weso.computex.profile.VReport._
import es.weso.utils.JenaUtils
import com.hp.hpl.jena.rdf.model.ModelFactory
/**
 * Contains a validation query. 
 * Validation queries have a name and a SPARQL CONSTRUCT query which constructs
 * an error model in case the model does not pass.
 * 
 */
case class Profile(
    val ontologyBase 	: URI,
    val validators		: Seq[Validator],
    val expanders		: Seq[Expander],
    val imports			: Seq[(URI,Profile)],
    val name			: String = "",
    val uri				: String = ""
    ) {


  val modelBase : Model = {
    JenaUtils.parseFromURI(ontologyBase.toString)
  }

  /*
   * Retrieves all the validators from the imported profiles
   */
  lazy val allValidators : Seq[Validator] = {
    validators ++ imports.map(pair => pair._2.allValidators).flatten
  }
  
  /*
   * Retrieves all the expanders from the imported profiles
   */
  lazy val allExpanders : Seq[Expander] = {
    expanders ++ imports.map(pair => pair._2.allExpanders).flatten
  }

  /**
   *  Expands a model using this profile
   */
  def expand(model:Model) : Model = {
    allExpanders.foldLeft(model)(combineExpansion)
  }

  private def combineExpansion(model: Model, expander: Expander) : Model = {
    expander.expand(model) match {
      case None => 
        throw 
        new Exception("Cannot expand model " + model + " with expander " + expander)
      case Some(newModel) => newModel
    }
  }

  
  /**
   *  Validates a model using this profile. Returns a validation report and 
   *     the model that has been evaluated
   *  @param Model to validate
   *  @param expandBefore if true applies expanders before validation
   *  @param imports if true applies imports 
   */
  def validate(model:Model, 
		  		expandBefore: Boolean = true, 
		  		imports: Boolean = true) : 
	  (VReport,Model) = {
    val expanded = 
    	if (expandBefore) {
    	  ModelFactory.createRDFSModel(modelBase, expand(model))
    	} else model
    val vals 	 = if (imports) allValidators
    			   else validators
    ((vals.foldLeft(Passed(Seq()) : VReport)(combineReport(expanded))),expanded)
  }

  /*
   * Combines a report with the result of a validator applied to a model
   * It takes into account which validators have passed and which didn't
   */
  private def combineReport(model: Model)
    	(r: VReport, v: Validator) : 
    		VReport = {
      v.validate(model) match {
        case Passed(v) => r match {
          case Passed(vs) => 
            Passed(v +: vs)
          case NotPassed(m,(vs,nvs)) => 
            NotPassed(m,(v +: vs,nvs))
        }
        case NotPassed(m1,v) => r match {
          case Passed(vs) => 
            NotPassed(m1, (vs, Seq(v)))
          case NotPassed(m2,(vs,nvs)) => 
            NotPassed(m1.add(m2),(vs, v +: nvs))
        }
      }
  }

  override def toString : String = {
    "Profile. Name = " + name + " URI = " + uri + 
    "\nValidators:" + validators + 
    "\nExpanders:" + expanders + 
    "\nImports: " + imports
  }

}

object Profile {
  
  val conf 				= ConfigFactory.load()
    
  def Cube : Profile = {
    val cubeProfile 	= ConfigUtils.getName(conf, "cubeProfile")
    val contents 		= Source.fromFile(cubeProfile).mkString
    val model 			= parseFromString(contents)
    ProfileParser.fromModel(model)(0)
  }

  def Computex : Profile = {
    val computexProfile = ConfigUtils.getName(conf, "computexProfile")
    val contents 		= Source.fromFile(computexProfile).mkString
    val model			= parseFromString(contents)
    ProfileParser.fromModel(model)(0)
  }
  
  def getProfile(name: String): Option[Profile] = {
    name match {
      case "RDF Data Cube" 	=> Some(Cube)
      case "Computex" 		=> Some(Computex)
      case _ 				=> None
    }
    
  }
}
    
    

