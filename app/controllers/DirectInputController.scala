package controllers

import java.io.ByteArrayInputStream

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

object DirectInputController 
	extends Controller 
	with Base {
  
  case class DirectInput(
      val content: Option[String], 
      val format: Option[String], 
      val ss: Option[Int], 
      val verbose: Option[Int], 
      val expand: Option[Int]
      )

  val directInputForm: Form[DirectInput] = Form(
    mapping(
      "fragment" -> optional(text),
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))(DirectInput.apply)(DirectInput.unapply))

  def byDirectInputGET() = Action {
    implicit request =>
      val message = CMessage(CMessage.DirectInput)
      message.message = MsgEmpty
      Ok(views.html.input.defaultInputGET(message))
  }

  def byDirectInputPOST() = Action {
    implicit request =>
      var message = CMessage(CMessage.DirectInput)
      directInputForm.bindFromRequest.fold(
        errors => {
          message.message = MsgBadFormed
          BadRequest(views.html.input.defaultInputGET(message))
        },
        directInput => {
          message.content = directInput.content.getOrElse(null)
          if (message.content != null) {
            message.contentFormat = directInput.format.getOrElse(Turtle)
            message.contentIS = new ByteArrayInputStream(message.content.getBytes("UTF-8"))
            message.ss = directInput.ss.getOrElse(0) != 0
            message.verbose = directInput.verbose.getOrElse(0) != 0
            message.expand = directInput.expand.getOrElse(0) != 0
            message = validateStream(message)
            Ok(views.html.generic.format(message))
          } else {
            message.message = MsgEmpty
            Ok(views.html.input.defaultInputGET(message))
          }
        })
  }
  
  
}