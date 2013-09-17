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
import es.weso.utils.JenaUtils._
import es.weso.computex.entities.MsgError
import es.weso.computex.entities.Message
import es.weso.utils.Parsed
import es.weso.utils.NotParsed
import es.weso.computex.entities.Options

object FileUploadController extends Controller with Base {
  
  case class FileInput(
      _profile: Option[String], 
      _syntax: Option[String], 
      _showSource: Option[Int], 
      _verbose: Option[Int], 
      _expand: Option[Int],
      _prefix: Option[Int]
      ) extends BaseInput(_profile,_syntax,_showSource,_verbose,_expand,_prefix)
    
  val fileInputForm: Form[FileInput] = Form(
    mapping(
      "doctype" -> optional(text),
      "profile" -> optional(text),
      "showSource" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number),
      "prefix" -> optional(number)      
      )
      (FileInput.apply)
      (FileInput.unapply))
  
  
  def byFileUploadGET() = Action {
    implicit request =>
      val message = CMessage(ByFile,MsgEmpty,Options())
      Ok(views.html.file.defaultFileGET(message))
  }

def byFileUploadPOST() = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("uploaded_file").map { file =>
        fileInputForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.file.defaultFileGET(CMessage(ByFile,MsgBadFormed,Options())))
          },
          fileInput => try {
            val opts = handleOptions(fileInput)
            import java.io.File
            val filename 		= file.filename
            val contentType 	= file.contentType
            val input 			= new ByteArrayInputStream(FileUtils.readFileToByteArray(file.ref.file))
            parseInputStream(input,"",opts.contentFormat) match {
              case Parsed(model) => {
                   val message = CMessage(ByFile,Message.validate(opts.profile,model,opts.expand),opts)
            	   Ok(views.html.generic.format(message))
                  }
              case NotParsed(err) => {
                   val msg = CMessage(ByFile,MsgError("Error parsing file: " + err),opts)
              	   BadRequest(views.html.input.defaultInputGET(msg))
              }
            }
          } catch {
          case e: Exception => 
              BadRequest(views.html.input.defaultInputGET(CMessage(ByFile,MsgError("Exception: " + e))))
          }
       )
      }.getOrElse {
        Ok(views.html.file.defaultFileGET(CMessage(ByFile,MsgEmpty)))
      }
  }
}