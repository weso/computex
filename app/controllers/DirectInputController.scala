package controllers

import java.io.ByteArrayInputStream

import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.MsgBadFormed
import es.weso.computex.entities.CMessage.MsgEmpty
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller

object DirectInputController extends Controller with Base {

  case class DirectInput(val content: Option[String], _format: Option[String],
    _ss: Option[Int], _verbose: Option[Int], _expand: Option[Int],
    _preffix: Option[Int]) extends BaseInput(_format, _ss, _verbose, _expand,
    _preffix)

  val directInputForm: Form[DirectInput] = Form(
    mapping(
      "fragment" -> optional(text),
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number),
      "preffix" -> optional(number))(DirectInput.apply)(DirectInput.unapply))

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
            message = handleOptions(message, directInput)
            message.contentIS = new ByteArrayInputStream(message.content.getBytes("UTF-8"))
            message = validateStream(message)
            Ok(views.html.generic.format(message))
          } else {
            message.message = MsgEmpty
            Ok(views.html.input.defaultInputGET(message))
          }
        })
  }

}