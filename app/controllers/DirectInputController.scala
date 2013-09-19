package controllers

import java.io.ByteArrayInputStream
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage._
import es.weso.utils.JenaUtils.Turtle
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.computex.entities.Action._
import es.weso.computex.entities.MsgEmpty
import es.weso.computex.entities.MsgBadFormed
import es.weso.computex.profile.Profile
import es.weso.computex.entities.MsgError
import es.weso.utils.JenaUtils
import es.weso.utils.JenaUtils._
import es.weso.computex.entities.Message
import es.weso.utils.Parsed
import es.weso.utils.NotParsed
import es.weso.computex.entities.Options

object DirectInputController 
	extends Controller 
	with Base {

case class DirectInput(
    content: Option[String],
    _profile: Option[String],
    _syntax: Option[String],
    _showSource: Option[Int], 
    _verbose: Option[Int], 
    _expand: Option[Int],
    _prefix: Option[Int]) 
    	extends 
    	BaseInput(
    	    _profile, 
    	    _syntax, 
    	    _showSource, 
    	    _verbose, 
    	    _expand, 
    	    _prefix
    	    )
  
  val directInputForm: Form[DirectInput] = Form(
    mapping(
      "fragment" 	-> optional(text),
      "profile" 	-> optional(text),
      "doctype" 	-> optional(text),
      "showSource" 	-> optional(number),
      "verbose" 	-> optional(number),
      "expand" 		-> optional(number),
      "prefix" 		-> optional(number)
      )
      (DirectInput.apply)
      (DirectInput.unapply))

  def byDirectInputGET() = Action {
    implicit request =>
      val message = CMessage(ByDirectInput,MsgEmpty,Options())
      Ok(views.html.input.defaultInputGET(message))
  }

  def byDirectInputPOST() = Action {
     implicit request =>
      directInputForm.bindFromRequest.fold(
        errors => {
          val msg = CMessage(ByDirectInput,MsgBadFormed,Options())
          BadRequest(views.html.generic.format(msg))
        },
        directInput => try {
          val opts  = handleOptions(directInput)
          directInput.content match {
            case None => 
              	val msg = CMessage(ByDirectInput,MsgError("Empty input"),opts,"")
            	Ok(views.html.generic.format(msg))
            case Some(content) => {
            	val model = parseFromString(content,"",opts.contentFormat) 
                val message = CMessage(ByDirectInput,Message.validate(opts.profile,model,opts.expand),opts,content)
            	Ok(views.html.generic.format(message))
            }
          }
         } catch {
            case e: Exception => 
              { val msg = CMessage(ByDirectInput,MsgError("Error: " + e),Options(),directInput.content.getOrElse(""))
              	BadRequest(views.html.generic.format(msg))
              }
          } 	 
       )
  }
  
  
}