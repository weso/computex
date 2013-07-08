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
import org.apache.commons.io.FileUtils

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

  def byUriGET(uriOpt: Option[String]) = Action {
    implicit request =>
      var message = CMessage(URI)
      uriForm.bindFromRequest.fold(
        errors => {
          message.message = MSG_BAD_FORMED;
          BadRequest(views.html.uri.defaultUriGET(message))
        },
        uriPath => {
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
                message.message = MSG_404
              case e: IOException =>
                message.message = MSG_404
            }
            Ok(views.html.generic.format(message))
          } else {
            Ok(views.html.uri.defaultUriGET(message))
          }
        })
  }

  def byDirectInputGET() = Action {
    implicit request =>
      val message = CMessage(URI)
      message.message = MSG_EMPTY
      Ok(views.html.input.defaultInputGET(message))
  }

  def byDirectInputPOST() = Action {
    implicit request =>
      var message = CMessage(DIRECT_INPUT)
      directInputForm.bindFromRequest.fold(
        errors => {
          message.message = MSG_BAD_FORMED
          BadRequest(views.html.input.defaultInputGET(message))
        },
        inputForm => {
          message.content = inputForm.content.getOrElse(null)
          if (message.content != null) {
            message.contentIS = new ByteArrayInputStream(message.content.getBytes("UTF-8"))
            message = validateStream(message)
            Ok(views.html.generic.format(message))
          } else {
            message.message = MSG_EMPTY
            Ok(views.html.input.defaultInputGET(message))
          }
        })
  }

  def byFileUploadGET() = Action {
    implicit request =>
      val message = CMessage(FILE)
      message.message = MSG_EMPTY
      Ok(views.html.file.defaultFileGET(message))
  }

  def byFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      var message = CMessage(FILE)
      request.body.file("uploaded_file").map { file =>
        import java.io.File
        val filename = file.filename
        val contentType = file.contentType
        message.content = file.filename
        message.contentIS = new ByteArrayInputStream(FileUtils.readFileToByteArray(file.ref.file))
        message = validateStream(message)
        Ok(views.html.generic.format(message))
      }.getOrElse {
        val message = CMessage(FILE)
        message.message = MSG_EMPTY
        Ok(views.html.file.defaultFileGET(message))
      }
  }

  private def validateStream(message: CMessage)(implicit request: RequestHeader): CMessage = {
    val conf: Config = ConfigFactory.load()
    val validationDir = conf.getString("validationDir")
    val ontologyURI = conf.getString("ontologyURI")
    val closureFile = conf.getString("closureFile")
    val flattenFile = conf.getString("flattenFile")
    println("CLOSURE " + closureFile)
    val cex = Computex(ontologyURI, validationDir, closureFile, flattenFile)

    try {
      val model = cex.computex(message.contentIS)

      val errors = Parser.parseErrors(model)

      message.errorMessages = for {
        i <- errors
        out = new ByteArrayOutputStream()
      } yield {
        i.model.write(out, TURTLE)
        (i.message, out.toString())
      }

      if (message.errorMessages.size > 0) {
        message.message = MSG_ERROR
      }

    } catch {
      case e: AtlasException => message.message = MSG_BAD_FORMED
      case e: RiotException => message.message = MSG_BAD_FORMED
    }
    message
  }
}