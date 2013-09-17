package controllers

import org.apache.jena.atlas.AtlasException
import org.apache.jena.riot.RiotException

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import es.weso.computex.Computex
import es.weso.computex.entities.CMessage
import play.api.mvc.Controller
import play.api.mvc.RequestHeader

trait Base extends Controller {

  def validateMessage
  			(message: CMessage)
  			(implicit request: RequestHeader): CMessage = {

    ???
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
  }
}

