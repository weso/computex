package controllers

import org.apache.jena.atlas.AtlasException
import org.apache.jena.riot.RiotException
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import es.weso.computex.Computex
import es.weso.computex.entities.CMessage
import es.weso.utils.JenaUtils.Turtle
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import es.weso.utils.JenaUtils.Turtle
import es.weso.computex.profile.Profile
import es.weso.computex.entities.Options

trait Base extends Controller {

  abstract class BaseInput(
      val profile:		Option[String],
      val format: 		Option[String], 
      val showSource: 	Option[Int], 
      val verbose: 		Option[Int], 
      val expand: 		Option[Int], 
      val prefix: 		Option[Int]
  )
  
  def validateMessage
  			(message: CMessage)
  			(implicit request: RequestHeader): CMessage = {
    ???
  }
    /*
    try {
      message.profile match {
        case None 		=> message.setError("Profile not initialized")
        case Some(prof) => {
          message.getModel match {
            case Some(m) => {
              val result = prof.validate(m, message.expand, true)
              message.setResult(result) 
            }
            case None => 
              message.setError("Cannot parse model")
          }
        }
      }
    } catch {
      case e: AtlasException => 
        message.setError(s"Bad formed as ${message.contentFormat}<br/>${e.getMessage}")
      case e: RiotException => 
        message.setError(s"Bad formed as ${message.contentFormat}<br/>${e.getMessage}")
    }
  }
 */

   def handleOptions(base: BaseInput): Options = {
    val profileStr 	= base.profile.getOrElse("Cube")
    Profile.getProfile(profileStr) match {
      case None => throw new Exception("Unknown profile " + profileStr)
      case Some(p) => {
    	  val syntax 	= base.format.getOrElse(Turtle)
    	  val ss 		= base.showSource.getOrElse(0) != 0
          val verb	 	= base.verbose.getOrElse(0) != 0
          val exp		= base.expand.getOrElse(0) != 0
          val pref 		= base.prefix.getOrElse(0) != 0
          Options(p,syntax,ss,verb,exp,pref)
      }
    }
  }
   
}

