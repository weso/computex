package controllers

import java.io.ByteArrayInputStream
import org.apache.commons.io.FileUtils
import es.weso.computex.entities.CMessage
import es.weso.utils.JenaUtils.Turtle
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.computex.entities.ByFile
import es.weso.computex.entities.MsgEmpty
import es.weso.computex.entities.MsgBadFormed
import es.weso.computex.entities.MsgOK
import es.weso.computex.profile.Profile

object FileUploadController extends Controller with Base {
  
  case class FileInput(
      val doctype: Option[String], 
      val profile: Option[String], 
      val showSource: Option[Int], 
      val verbose: Option[Int], 
      val expand: Option[Int])
    
  val fileInputForm: Form[FileInput] = Form(
    mapping(
      "doctype" -> optional(text),
      "profile" -> optional(text),
      "showSource" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))
      (FileInput.apply)
      (FileInput.unapply))
  
  
  def byFileUploadGET() = Action {
    implicit request =>
      val message = CMessage(ByFile,MsgEmpty)
      Ok(views.html.file.defaultFileGET(message))
  }

def byFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("uploaded_file").map { file =>
        fileInputForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.file.defaultFileGET(CMessage(ByFile,MsgBadFormed)))
          },
          fileInput => {
            import java.io.File
            val filename 		= file.filename
            val contentType 	= file.contentType
            val contentFormat 	= fileInput.doctype.getOrElse(Turtle)
            val profile 		= fileInput.profile.getOrElse("Computex")
            val contentIS 		= new ByteArrayInputStream(FileUtils.readFileToByteArray(file.ref.file))
            val contentString 	= CMessage.is2str(contentIS) 
            val showSource 		= fileInput.showSource.getOrElse(0) != 0
            val verbose 		= fileInput.verbose.getOrElse(0) != 0
            val expand 			= fileInput.expand.getOrElse(0) != 0
            val message = CMessage(
            			 ByFile,
                		 MsgOK,
                		 Some(contentString),
                		 Profile.getProfile(profile),
            			 contentFormat,
            			 verbose,
            			 showSource,
            			 expand)
             val newMessage = validateMessage(message)                		
             Ok(views.html.generic.format(newMessage))
          })

      }.getOrElse {
        Ok(views.html.file.defaultFileGET(CMessage(ByFile,MsgEmpty)))
      }
  }
}