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
import es.weso.computex.entities.Action._
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
  case class UriInput(
      val uri: 			Option[String], 
      val _syntax: 		Option[String], 
      val _profile: 		Option[String],
      val _showSource: 	Option[Int], 
      val _verbose: 		Option[Int], 
      val _expand: 		Option[Int],
      val _prefix: 		Option[Int]
      ) extends 
    	BaseInput(
    	    _profile, 
    	    _syntax, 
    	    _showSource, 
    	    _verbose, 
    	    _expand, 
    	    _prefix
    	    )

  
  val uriForm: Form[UriInput] = Form(
    mapping(
      "uri" 		-> optional(text),
      "doctype" 	-> optional(text),
      "profile" 	-> optional(text),
      "showSource" 	-> optional(number),  // TODO: should be booleans
      "verbose" 	-> optional(number),
      "expand" 		-> optional(number),
      "prefix" 		-> optional(number)
      )
      (UriInput.apply)
      (UriInput.unapply)
    )

  def byUriGET(uriOpt: Option[String]) = Action {
    implicit request =>
      uriForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.uri.defaultUriGET(CMessage(ByURI,MsgBadFormed)))
        },
        uriInput => {
          uriInput.uri match {
            case None =>
                Ok(views.html.uri.defaultUriGET(CMessage(ByURI,MsgEmpty)))

            case Some(uri) =>
            try {
              val opts = handleOptions(uriInput)
              parseInputStream(dereferenceURI(uri),"",opts.contentFormat) match {
                case Parsed(model) => {
                    val message = CMessage(ByURI,Message.validate(opts.profile,model,opts.expand),opts,uri)
            	    Ok(views.html.generic.format(message))
                }
                case NotParsed(err) => {
           	        val msg = CMessage(ByURI,MsgError("Error parsing: " + err))
           	        BadRequest(views.html.uri.defaultUriGET(msg))
                }
            }
            } catch {
              case e: Exception =>
              	val msg = CMessage(ByURI,MsgError("Error: " + e + " with URI " + uri))
           	    BadRequest(views.html.uri.defaultUriGET(msg))              }
           }
         })
  }
}