package controllers

import java.io.FileNotFoundException
import java.io.IOException
import es.weso.computex.Computex
import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.Msg404
import es.weso.computex.entities.CMessage.MsgBadFormed
import es.weso.computex.entities.CMessage.MsgEmpty
import es.weso.computex.entities.CMessage.Uri
import es.weso.utils.JenaUtils._
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.utils.JenaUtils

object URIController extends Controller with Base {

  // TODO: change to boolean values showSource, verbose, etc
  case class UriPath(
      val uri: Option[String], 
      val format: Option[String], 
      val profile: Option[String],
      val showSource: Option[Int], 
      val verbose: Option[Int], 
      val expand: Option[Int]
      )
  
  val uriForm: Form[UriPath] = Form(
    mapping(
      "uri" 		-> optional(text),
      "doctype" 	-> optional(text),
      "profile" 	-> optional(text),
      "showSource" 	-> optional(number),  // TODO: should be booleans
      "verbose" 	-> optional(number),
      "expand" 		-> optional(number))
      (UriPath.apply)
      (UriPath.unapply)
    )

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
            message.content = uri 
            // TODO: I removed the following code. Check behaviour with relative URIs
            /* if (!uri.startsWith("http://")) {
              if (!uri.startsWith("https://")) {
                "http://" + uri
              } else { uri }
            } else { uri } */
            try {
              message.contentIS = JenaUtils.dereferenceURI(message.content)
              message.profile = uriPath.profile.getOrElse("Computex")
              message.contentFormat = uriPath.format.getOrElse(Turtle)
              message.showSource = uriPath.showSource.getOrElse(0) != 0
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