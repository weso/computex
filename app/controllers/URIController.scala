package controllers

import java.io.FileNotFoundException
import java.io.IOException
import es.weso.computex.Computex
import es.weso.computex.entities.CMessage
import es.weso.utils.JenaUtils._
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.utils.JenaUtils
import es.weso.computex.entities.ByURI
import es.weso.computex.entities.MsgBadFormed
import es.weso.computex.profile.Profile
import es.weso.computex.entities.Msg404
import es.weso.computex.entities.MsgEmpty
import es.weso.computex.entities.MsgError
import es.weso.computex.entities.Message
import es.weso.utils.Parsed
import es.weso.utils.NotParsed

object URIController extends Controller with Base {

  // TODO: change to boolean values showSource, verbose, etc
  case class UriPath(
      val uri: 			Option[String], 
      val format: 		Option[String], 
      val profile: 		Option[String],
      val showSource: 	Option[Int], 
      val verbose: 		Option[Int], 
      val expand: 		Option[Int]
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
      uriForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.uri.defaultUriGET(CMessage(ByURI,MsgBadFormed)))
        },
        uriPath => {
          val uri = uriPath.uri.getOrElse(null)
          if (uri != null) {
            // TODO: I removed the following code. Check behaviour with relative URIs
            /* if (!uri.startsWith("http://")) {
              if (!uri.startsWith("https://")) {
                "http://" + uri
              } else { uri }
            } else { uri } */
            try {
              val is 			= JenaUtils.dereferenceURI(uri)
              val profile 		= uriPath.profile.getOrElse("Computex")
              val contentFormat = uriPath.format.getOrElse(Turtle)
              val showSource 	= uriPath.showSource.getOrElse(0) != 0
              val verbose 		= uriPath.verbose.getOrElse(0) != 0
              val expand 		= uriPath.expand.getOrElse(0) != 0
              JenaUtils.parseInputStream(is,"",contentFormat) match {
                case Parsed(model) => 
                  Profile.getProfile(profile) match {
                   case None => { 
              	    val msg = CMessage(ByURI,MsgError("Unknown profile: " + profile))
              	    BadRequest(views.html.input.defaultInputGET(msg))
                   }
                  case Some(profile) => {
                    val message = CMessage(ByURI,Message.validate(profile,model,expand))
            	    Ok(views.html.generic.format(message))
                  }
                }
                case NotParsed(err) => {
           	        val msg = CMessage(ByURI,MsgError("Error parsing: " + err))
           	        BadRequest(views.html.input.defaultInputGET(msg))
                }
              }
            } catch {
              case e: FileNotFoundException =>
              	Ok(views.html.generic.format(CMessage(ByURI,Msg404)))
              case e: IOException =>
              	Ok(views.html.generic.format(CMessage(ByURI,Msg404)))
            }
          } else {
            Ok(views.html.uri.defaultUriGET(CMessage(ByURI,MsgEmpty)))
          }
        })
  }
}