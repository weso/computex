package controllers

import java.io.FileNotFoundException
import java.io.IOException

import es.weso.computex.Computex
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.Msg404
import es.weso.computex.entities.CMessage.MsgBadFormed
import es.weso.computex.entities.CMessage.MsgEmpty
import es.weso.computex.entities.CMessage.Uri
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller

object URIController extends Controller with Base {

  case class UriPath(val uri: Option[String], _format: Option[String],
    _ss: Option[Int], _verbose: Option[Int], _expand: Option[Int],
    _preffix: Option[Int]) extends BaseInput(_format, _ss, _verbose, _expand,
    _preffix)
  
  val uriForm: Form[UriPath] = Form(
    mapping(
      "uri" -> optional(text),
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number),
      "preffix" -> optional(number))(UriPath.apply)(UriPath.unapply))

  def byUriGET(uriOpt: Option[String]) = Action {
    implicit request =>
      var message = CMessage(Uri)
      uriForm.bindFromRequest.fold(
        errors => {
          message.message = MsgBadFormed;
          println(errors)
          BadRequest(views.html.uri.defaultUriGET(message))
        },
        uriPath => {
          val uri = uriPath.uri.getOrElse(null)
          if (uri != null) {
            message.content = if (!uri.startsWith("http://")) {
              if (!uri.startsWith("https://")) {
                "http://" + uri
              } else { uri }
            } else { uri }
            try {
              message = handleOptions(message, uriPath)
              message.contentIS = Computex.loadFile(message.content)
              message = validateStream(message)
            } catch {
              case e: FileNotFoundException =>
                message.message = Msg404
              case e: IOException =>
                message.message = Msg404
            }
            Ok(views.html.generic.format(message))
          } else {
            message.message = MsgEmpty
            Ok(views.html.uri.defaultUriGET(message))
          }
        })
  }
  
}