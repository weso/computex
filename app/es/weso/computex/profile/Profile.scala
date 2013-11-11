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
import org.slf4j.LoggerFactory
import es.weso.utils.ComputeTools

/**
 * Contains a validation query. 
 * Validation queries have a name and a SPARQL CONSTRUCT query which constructs
 * an error model in case the model does not pass.
 * 
 */
case class Profile(
    val ontologyBase 	: Option[URI],
    val validators		: Seq[Validator],
    val computeSteps	: Seq[ComputeStep],
    val imports			: Seq[(URI,Profile)],
    val name			: String = "",
    val uri				: String = ""
    ) {


  val logger 		= LoggerFactory.getLogger("Application")
  
  val modelBase : Model = {
    ontologyBase match {
      case Some(base) => JenaUtils.parseFromURI(base.toString)
      case None => ModelFactory.createDefaultModel()
    }
  }

  /*
   * Retrieves all the validators from the imported profiles
   */
  lazy val allValidators : Seq[Validator] = {
    validators ++ imports.map(pair => pair._2.allValidators).flatten 
  }
  
  lazy val allComputeSteps : Seq[ComputeStep] = {
    computeSteps ++ imports.map(pair => pair._2.allComputeSteps).flatten 
  }

  /**
   *  Expands a model using this profile
   *  It applies computation steps 
   */
  def expand(model:Model) : Model = {
    val merge   : (Model,Model) => Model = (m1, m2) => m1.add(m2)
    val steps   : Seq[Model => Model] 	 = allComputeSteps.map(x => (m => x.step(m)))
    val initial : Model 				 = model
    ComputeTools.compute(steps, initial, merge)
  }

  def expandStep(stepName: String, model:Model) : Model = {
    allComputeSteps.find(c => c.name == stepName) match {
      case Some(cstep) => {
        val newModel = cstep.step(model)
        println("Constructed model of size: " + newModel.size)
        model.add(newModel)
      }
      case None => throw new Exception("expandStep. stepName " + stepName + " not found")
    }
  }

  def expandDebug(model:Model) : Model = {
    val merge   : (Model,Model) => Model = (m1, m2) => { 
        println("Merging models. Size(M1) = " + m1.size + "Size(M2) = " + m2.size)
        m1.add(m2)
    }
    val steps   : Seq[Model => Model] 	 = 
      allComputeSteps.map(x => ((m : Model) => {
         println("ComputeStep: " + x.name + " over a model with size: " + m.size)
         x.step(m)
      }))
    val initial : Model 				 = model
    ComputeTools.compute(steps, initial, merge)
  }
  /**
   *  Compute a model using this profile. It returns
   *  a pair of models: the expanded model and the computed model
   */
  def compute(model:Model) : (Model,Model) = {
    val merge   : (Model,Model) => Model = (m1, m2) => m1.add(m2)
    val steps   : Seq[Model => Model] 	 = allComputeSteps.map(x => (m => x.step(m)))
    val initial : Model 				 = model
    val (expanded, results) = ComputeTools.computeDebug(steps, initial, merge)
    val resultsMerged = results.foldLeft(JenaUtils.emptyModel)(merge)
    (expanded,resultsMerged)
  }

  private def info(msg : String) : Unit = {
    println(msg)
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
          case NotPassed((vs,nvs)) => 
            NotPassed((v +: vs,nvs))
        }
        case NotPassed((v,m1)) => r match {
          case Passed(vs) => 
            NotPassed((vs, Seq((v,m1))))
          case NotPassed((vs,nvs)) => 
            NotPassed((vs, (v,m1) +: nvs))
        }
      }
  }

  override def toString : String = {
    "Profile. Name = " + name + " URI = " + uri + 
    "\nValidators:" + validators + 
    "\nComputeSteps:" + computeSteps + 
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
  
  def RDFSchema : Profile = {
    val rdfSchemaProfile = ConfigUtils.getName(conf, "rdfSchemaProfile")
    val contents 		= Source.fromFile(rdfSchemaProfile).mkString
    val model			= parseFromString(contents)
    ProfileParser.fromModel(model)(0)
  }

  def getProfile(name: String): Option[Profile] = {
    name match {
      case "Cube" 			=> Some(Cube)
      case "RDF Data Cube" 	=> Some(Cube)
      case "Computex" 		=> Some(Computex)
      case _ 				=> None
    }
    
  }
  
  def profiles : Seq[String] = Seq("Cube", "Computex")

}
    
    

