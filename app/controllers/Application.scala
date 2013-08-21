package controllers

import es.weso.computex.entities.CMessage
import es.weso.computex.entities.CMessage.MsgEmpty
import es.weso.computex.entities.CMessage.Uri
import play.api.mvc.Action
import play.api.mvc.Controller

object Application extends Controller {

  def index = Action {
    implicit request =>
      val message = CMessage(Uri)
      message.message = MsgEmpty
      Ok(views.html.index(message))
  }

  def about() = Action {
    implicit request =>
      Ok(views.html.about.about(CMessage(CMessage.File)))
  }
  
}