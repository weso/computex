package controllers

import play.api._
import play.api.mvc._
import com.typesafe.config._
import es.weso.computex.Parser
import es.weso.computex.Computex
import es.weso.utils.JenaUtils.TURTLE
import es.weso.computex.entities.CErrorMessage
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

case class UriPath(val uri: Option[String], val format: Option[String], val ss: Option[Int], val verbose: Option[Int], val expand: Option[Int])
case class DirectInput(val content: Option[String], val format: Option[String], val ss: Option[Int], val verbose: Option[Int], val expand: Option[Int])
case class FileInput(val format: Option[String], val ss: Option[Int], val verbose: Option[Int], val expand: Option[Int])

object Application extends Controller {

  val URI = "URI"
  val DIRECT_INPUT = "DIRECT_IMPUT"

  val uriForm: Form[UriPath] = Form(
    mapping(
      "uri" -> optional(text),
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))(UriPath.apply)(UriPath.unapply))

  val directInputForm: Form[DirectInput] = Form(
    mapping(
      "fragment" -> optional(text),
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))(DirectInput.apply)(DirectInput.unapply))

  val fileInputForm: Form[FileInput] = Form(
    mapping(
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))(FileInput.apply)(FileInput.unapply))

  def index = Action {
    implicit request =>
      val message = CMessage(URI)
      message.message = MSG_EMPTY
      Ok(views.html.index(message))
  }

  def byUriGET(uriOpt: Option[String]) = Action {
    implicit request =>
      var message = CMessage(URI)
      uriForm.bindFromRequest.fold(
        errors => {
          message.message = MSG_BAD_FORMED;
          println(errors)
          BadRequest(views.html.uri.defaultUriGET(message))
        },
        uriPath => {
          val uri = uriPath.uri.getOrElse(null)
          if (uri != null) {
            message.content = if (!uri.startsWith("http://")) {
              "http://" + uri
            } else { uri }
            try {
              message.contentIS = Computex.loadFile(message.content)
              message.contentFormat = uriPath.format.getOrElse(TURTLE)
              message.ss = uriPath.ss.getOrElse(0) != 0
              message.verbose = uriPath.verbose.getOrElse(0) != 0
              message.expand = uriPath.expand.getOrElse(0) != 0
              message = validateStream(message)
            } catch {
              case e: FileNotFoundException =>
                message.message = MSG_404
              case e: IOException =>
                message.message = MSG_404
            }
            Ok(views.html.generic.format(message))
          } else {
            message.message = MSG_EMPTY
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
        directInput => {
          message.content = directInput.content.getOrElse(null)
          if (message.content != null) {
            message.contentFormat = directInput.format.getOrElse(TURTLE)
            message.contentIS = new ByteArrayInputStream(message.content.getBytes("UTF-8"))
            message.ss = directInput.ss.getOrElse(0) != 0
            message.verbose = directInput.verbose.getOrElse(0) != 0
            message.expand = directInput.expand.getOrElse(0) != 0
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
        fileInputForm.bindFromRequest.fold(
          errors => {
            message.message = MSG_BAD_FORMED
            BadRequest(views.html.file.defaultFileGET(message))
          },
          fileInput => {
            import java.io.File
            val filename = file.filename
            val contentType = file.contentType
            message.content = file.filename
            message.contentFormat = fileInput.format.getOrElse(TURTLE)
            message.contentIS = new ByteArrayInputStream(FileUtils.readFileToByteArray(file.ref.file))
            message.ss = fileInput.ss.getOrElse(0) != 0
            message.verbose = fileInput.verbose.getOrElse(0) != 0
            message.expand = fileInput.expand.getOrElse(0) != 0
            message = validateStream(message)
            Ok(views.html.generic.format(message))
          })

      }.getOrElse {
        val message = CMessage(FILE)
        message.message = MSG_EMPTY
        Ok(views.html.file.defaultFileGET(message))
      }
  }

  def about() = Action {
    implicit request =>
      Ok(views.html.about.about(CMessage(FILE)))
  }

  private def validateStream(message: CMessage)(implicit request: RequestHeader): CMessage = {
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
        message.message = MSG_ERROR
      }

    } catch {
      case e: AtlasException => message.message = MSG_BAD_FORMED + " as " + message.contentFormat
      case e: RiotException => message.message = MSG_BAD_FORMED + " as " + message.contentFormat
    }
    message
  }
}