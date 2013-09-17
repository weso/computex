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
import es.weso.computex.profile.Profile
import es.weso.utils.JenaUtils
import es.weso.computex.entities.MsgError
import es.weso.computex.entities.Message
import es.weso.utils.Parsed
import es.weso.utils.NotParsed

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
            val input 			= new ByteArrayInputStream(FileUtils.readFileToByteArray(file.ref.file))
            val showSource 		= fileInput.showSource.getOrElse(0) != 0
            val verbose 		= fileInput.verbose.getOrElse(0) != 0
            val expand 			= fileInput.expand.getOrElse(0) != 0
            
            JenaUtils.parseInputStream(input,"",contentFormat) match {
              case Parsed(model) => 
                Profile.getProfile(profile) match {
                  case None => { 
              	   val msg = CMessage(ByFile,MsgError("Unknown profile: " + profile))
              	   BadRequest(views.html.input.defaultInputGET(msg))
                  }
                  case Some(profile) => {
                   val message = CMessage(ByFile,Message.validate(profile,model,expand))
            	   Ok(views.html.generic.format(message))
                  }
                }
              case NotParsed(err) => {
                   val msg = CMessage(ByFile,MsgError("Error parsing: " + err))
              	   BadRequest(views.html.input.defaultInputGET(msg))
              }
            }
          })

      }.getOrElse {
        Ok(views.html.file.defaultFileGET(CMessage(ByFile,MsgEmpty)))
      }
  }
}