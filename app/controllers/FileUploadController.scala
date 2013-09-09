package controllers

import java.io.ByteArrayInputStream

import org.apache.commons.io.FileUtils

import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.MsgBadFormed
import es.weso.computex.entities.CMessage.MsgEmpty
import es.weso.utils.JenaUtils.Turtle
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller

object FileUploadController extends Controller with Base {
  
  case class FileInput(val format: Option[String], val showSource: Option[Int], val verbose: Option[Int], val expand: Option[Int])
    
  val fileInputForm: Form[FileInput] = Form(
    mapping(
      "doctype" -> optional(text),
      "showSource" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))(FileInput.apply)(FileInput.unapply))
  
  
  
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
            val filename = file.filename
            val contentType = file.contentType
            message.content = file.filename
            message.contentFormat = fileInput.format.getOrElse(Turtle)
            message.contentIS = new ByteArrayInputStream(FileUtils.readFileToByteArray(file.ref.file))
            message.showSource = fileInput.showSource.getOrElse(0) != 0
            message.verbose = fileInput.verbose.getOrElse(0) != 0
            message.expand = fileInput.expand.getOrElse(0) != 0
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