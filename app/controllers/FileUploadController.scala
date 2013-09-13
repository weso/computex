package controllers

import java.io.ByteArrayInputStream

import org.apache.commons.io.FileUtils

import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.MsgBadFormed
import es.weso.computex.entities.CMessage.MsgEmpty
import es.weso.utils.JenaUtils.Turtle
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller

object FileUploadController extends Controller with Base {

  case class FileInput(_format: Option[String], _ss: Option[Int],
    _verbose: Option[Int], _expand: Option[Int], _preffix: Option[Int])
    extends BaseInput(_format, _ss, _verbose, _expand, _preffix)

  val fileInputForm: Form[FileInput] = Form(
    mapping(
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number),
      "preffix" -> optional(number))(FileInput.apply)(FileInput.unapply))

  def byFileUploadGET() = Action {
    implicit request =>
      val message = CMessage(CMessage.File)
      message.message = MsgEmpty
      Ok(views.html.file.defaultFileGET(message))
  }

  def byFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      var message = CMessage(CMessage.File)
      request.body.file("uploaded_file").map { file =>
        fileInputForm.bindFromRequest.fold(
          errors => {
            message.message = MsgBadFormed
            BadRequest(views.html.file.defaultFileGET(message))
          },
          fileInput => {
            import java.io.File
            message = handleOptions(message, fileInput)
            message.content = file.filename
            message.contentIS = new ByteArrayInputStream(FileUtils.readFileToByteArray(file.ref.file))
            message = validateStream(message)
            Ok(views.html.generic.format(message))
          })

      }.getOrElse {
        val message = CMessage(CMessage.File)
        message.message = MsgEmpty
        Ok(views.html.file.defaultFileGET(message))
      }
  }
}