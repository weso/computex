package controllers

import org.apache.jena.atlas.AtlasException
import org.apache.jena.riot.RiotException

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import es.weso.computex.Computex
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.MsgBadFormed
import es.weso.computex.entities.CMessage.MsgError
import es.weso.utils.JenaUtils.Turtle
import play.api.mvc.Controller
import play.api.mvc.RequestHeader

trait Base extends Controller {

  abstract class BaseInput(val format: Option[String], val ss: Option[Int], val verbose: Option[Int], val expand: Option[Int], val preffix: Option[Int])

  def validateStream(message: CMessage)(implicit request: RequestHeader): CMessage = {
    val conf: Config = ConfigFactory.load()
    val validationDir = conf.getString("validationDir")
    val computationDir = conf.getString("computationDir")
    val ontologyURI = conf.getString("ontologyURI")
    val closureFile = conf.getString("closureFile")
    val flattenFile = conf.getString("flattenFile")
    val findStepsQuery = conf.getString("findStepsQuery")

    val cex = Computex(ontologyURI, validationDir, computationDir, closureFile, flattenFile, findStepsQuery)

    try {
      message.integrityQueries = cex.computex(message)
      if (message.size > 0) {
        message.message = MsgError
      }

    } catch {
      case e: AtlasException => message.message = s"${MsgBadFormed} as ${message.contentFormat}<br/>${e.getMessage}"
      case e: RiotException => message.message = s"${MsgBadFormed} as ${message.contentFormat}<br/>${e.getMessage}"
    }
    message
  }

  def handleOptions(message: CMessage, base: BaseInput) = {
    message.contentFormat = base.format.getOrElse(Turtle)
    message.ss = base.ss.getOrElse(0) != 0
    message.verbose = base.verbose.getOrElse(0) != 0
    message.expand = base.expand.getOrElse(0) != 0
    message.preffix = base.preffix.getOrElse(0) != 0
    message
  }

}

object Main extends Base {
  def main(args: Array[String]): Unit = {
    var message = CMessage(CMessage.Uri)
    message.contentIS = Computex.loadFile("http://localhost:9000/assets/example.ttl")
    message.contentFormat = es.weso.utils.JenaUtils.Turtle
    message.ss = true
    message.verbose = true
    message.expand = true
    message = validateStream(message)(null)
  }
}