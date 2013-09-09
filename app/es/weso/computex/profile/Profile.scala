package es.weso.computex.profile

import com.hp.hpl.jena.rdf.model.Model
import es.weso.utils.JenaUtils._
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.net.URI
import es.weso.computex.Passed
import es.weso.computex.ValidationReport
import es.weso.computex.NotPassed
import es.weso.computex.Expander
import es.weso.computex.Validator
import es.weso.utils.ConfigUtils
import scala.io.Source

/**
 * Contains a validation query. 
 * Validation queries have a name and a SPARQL CONSTRUCT query which constructs
 * an error model in case the model does not pass.
 * 
 */
case class Profile(
    val validators: Seq[Validator],
    val expanders: 	Seq[Expander],
    val imports: Seq[(URI,Profile)],
    val name: String = "",
    val uri: String = ""
    ) {

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
      case None => throw new Exception("Cannot expand model " + model + " with expander " + expander)
      case Some(newModel) => newModel
    }
  }

  
  /**
   *  Validates a model using this profile
   */
  def validate(model:Model) : ValidationReport[Model] = {
    allValidators.foldLeft(Passed : ValidationReport[Model])(combineReport(model))
  }

  /*
   * Combines a report with the result of a validator applied to a model
   */
  private def combineReport(model: Model)
    	(r: ValidationReport[Model], v: Validator) : 
    		ValidationReport[Model] = {
      v.validate(model) match {
        case Passed => r
        case NotPassed(m1) => r match {
          case Passed => Passed
          case NotPassed(m2) => NotPassed(m1.add(m2))
        }
      }
  }

  /*
   * Validate a model after its expansion
   */
  def validateExpanded(model: Model) : ValidationReport[Model] = {
    val expanded = expand(model)
    validate(expanded)
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
    val model 			= parseModel(contents).get
    ProfileParser.fromModel(model)(0)
  }

  def Computex : Profile = {
    val computexProfile = ConfigUtils.getName(conf, "computexProfile")
    val contents 		= Source.fromFile(computexProfile).mkString
    val model			= parseModel(contents).get
    ProfileParser.fromModel(model)(0)
  }
}
    
    

