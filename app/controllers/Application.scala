package controllers

import play.api._
import play.api.mvc._
import com.typesafe.config._
import es.weso.computex.Parser
import es.weso.computex.Computex
import es.weso.utils.JenaUtils.TURTLE
import es.weso.computex.entities.ErrorMessage
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage._
import java.io.File
import java.io.OutputStream
import java.io.Writer
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.FileInputStream
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import java.io.FileNotFoundException
import org.apache.jena.riot.RiotException
import java.io.IOException
import java.nio.charset.MalformedInputException
import org.apache.jena.atlas.AtlasException
import views.html.main

case class UriPath(val uri: Option[String])
case class DirectInput(val content: Option[String])

object Application extends Controller {

  val URI = "URI"
  val DIRECT_INPUT = "DIRECT_IMPUT"

  val uriForm: Form[UriPath] = Form(
    mapping(
      "uri" -> optional(text))(UriPath.apply)(UriPath.unapply))

  val directInputForm: Form[DirectInput] = Form(
    mapping(
      "fragment" -> optional(text))(DirectInput.apply)(DirectInput.unapply))

  def index = Action {
    implicit request =>
      Ok(views.html.index(CMessage(URI)))
  }

  def byUri(uriOpt: Option[String]) = Action {
    implicit request =>
      uriForm.bindFromRequest.fold(
        errors => BadRequest(main(":(")(views.html.uri.defaultUri(CMessage(URI)))),
        uriPath => {
          var message = CMessage(URI)
          val uri = uriPath.uri.getOrElse(null)
          if (uri != null) {
            message.content = if (!uri.startsWith("http://")) {
              "http://" + uri
            } else { uri }
            try {
              message.contentIS = Computex.loadFile(message.content);
              message = validateStream(message)
            } catch {
              case e: FileNotFoundException =>
                message.status = MSG_404
              case e: IOException =>
                message.status = MSG_404
            }
            Ok(views.html.generic.format(message))
          } else {
            Ok(main(":(")(views.html.uri.defaultUri(CMessage(URI))))
          }
        }
      )
  }

  def byDirectInputGet() = Action {
    implicit request =>
      println("ENTRA1")
      Ok(main(":(")(views.html.input.defaultInput(CMessage(URI))))
  }

  def byDirectInputPost() = Action {
    implicit request =>
      println("ENTRA2")
      directInputForm.bindFromRequest.fold(
        errors => BadRequest(main(":(")(views.html.input.defaultInput(CMessage(DIRECT_INPUT)))),
        inputForm => {
          var message = CMessage(DIRECT_INPUT)
          message.content = inputForm.content.getOrElse(null)
          if (message.content != null) {
            message.contentIS = new ByteArrayInputStream(message.content.getBytes("UTF-8"))
            message = validateStream(message)
            Ok(views.html.generic.format(message))
          } else {
            Ok(main(":(")(views.html.input.defaultInput(CMessage(DIRECT_INPUT))))
          }
        }
      )
  }

  private def validateStream(message: CMessage)(implicit request: RequestHeader): CMessage = {
    val conf: Config = ConfigFactory.load()
    val validationDir = conf.getString("validationDir")
    val ontologyURI = conf.getString("ontologyURI")

    val cex = new Computex

    try {
      val model = cex.computex(message.contentIS, ontologyURI, validationDir)

      val errors = Parser.parseErrors(model)

      message.errorMessages = for {
        i <- errors
        out = new ByteArrayOutputStream()
      } yield {
        i.model.write(out, TURTLE)
        (i.message, out.toString())
      }

      if (message.errorMessages.size > 0) {
        message.status = MSG_ERROR
      }

    } catch {
      case e: AtlasException => message.status = MSG_BAD_FORMED
      case e: RiotException => message.status = MSG_BAD_FORMED
    }
    message
  }
}