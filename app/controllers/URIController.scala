package controllers

import java.io.FileNotFoundException
import java.io.IOException

import es.weso.computex.Computex
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.Msg404
import es.weso.computex.entities.CMessage.MsgBadFormed
import es.weso.computex.entities.CMessage.MsgEmpty
import es.weso.computex.entities.CMessage.Uri
import es.weso.utils.JenaUtils.Turtle
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller

object URIController extends Controller with Base {

  case class UriPath(val uri: Option[String], val format: Option[String], val ss: Option[Int], val verbose: Option[Int], val expand: Option[Int])
  
  val uriForm: Form[UriPath] = Form(
    mapping(
      "uri" -> optional(text),
      "doctype" -> optional(text),
      "ss" -> optional(number),
      "verbose" -> optional(number),
      "expand" -> optional(number))(UriPath.apply)(UriPath.unapply))

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
              message.contentIS = Computex.loadFile(message.content)
              message.contentFormat = uriPath.format.getOrElse(Turtle)
              message.ss = uriPath.ss.getOrElse(0) != 0
              message.verbose = uriPath.verbose.getOrElse(0) != 0
              message.expand = uriPath.expand.getOrElse(0) != 0
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