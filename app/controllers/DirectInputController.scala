package controllers

import java.io.ByteArrayInputStream
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage._
import es.weso.utils.JenaUtils.Turtle
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.computex.entities.ByDirectInput
import es.weso.computex.entities.MsgEmpty
import es.weso.computex.entities.MsgBadFormed
import es.weso.computex.profile.Profile
import es.weso.computex.entities.MsgError
import es.weso.utils.JenaUtils
import es.weso.computex.entities.Message
import es.weso.utils.Parsed
import es.weso.utils.NotParsed


object DirectInputController 
	extends Controller 
	with Base {
  
case class DirectInput(
    val content: Option[String], 
    val profile: Option[String], 
    val format: Option[String], 
    val showSource: Option[Int], 
    val verbose: Option[Int], 
    val expand: Option[Int]
    )

  val directInputForm: Form[DirectInput] = Form(
    mapping(
      "fragment" -> optional(text),
      "profile" -> optional(text),
      "doctype" -> optional(text),
      "showSource" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))
      (DirectInput.apply)
      (DirectInput.unapply))

  def byDirectInputGET() = Action {
    implicit request =>
      val message = CMessage(ByDirectInput,MsgEmpty)
      Ok(views.html.input.defaultInputGET(message))
  }

  def byDirectInputPOST() = Action {
     implicit request =>
      directInputForm.bindFromRequest.fold(
        errors => {
          val msg = CMessage(ByDirectInput,MsgBadFormed)
          BadRequest(views.html.input.defaultInputGET(msg))
        },
        directInput => {
          directInput.content match {
            case None => 
            	Ok(views.html.input.defaultInputGET(CMessage(ByDirectInput,MsgEmpty)))
            case Some(content) => {
            	val profileStr 		= directInput.profile.getOrElse("Computex")
            	val contentFormat 	= directInput.format.getOrElse(Turtle)
                val verbose 		= directInput.verbose.getOrElse(0) != 0
                val showSource 		= directInput.showSource.getOrElse(0) != 0
                val expand 			= directInput.expand.getOrElse(0) != 0  
                // TODO: add toggle of imports            val imports			= directInput.imoprts.getOrElse(0) != 0  
                JenaUtils.str2Model(content,"",contentFormat) match {
            	  case Parsed(model) => 
            	     Profile.getProfile(profileStr) match {
                       case Some(profile) => {
                        val message = CMessage(ByDirectInput,Message.validate(profile,model,expand))
            	        println("Parsed! ")
            	        Ok(views.html.generic.format(message))
                       }
                      case None => { 
              	        val msg = CMessage(ByDirectInput,MsgError("Unknown profile: " + profileStr))
            	        BadRequest(views.html.input.defaultInputGET(msg))
                       }
            	     }
            	 case NotParsed(err) => {
            	        val msg = CMessage(ByDirectInput,MsgError("Error parsing: " + err))
              	        BadRequest(views.html.input.defaultInputGET(msg))
            	 }
            	}
             }
            }
          } 
        )
  }
  
  
}