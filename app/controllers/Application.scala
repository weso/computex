package controllers

import es.weso.computex.entities._
import play.api.mvc.Action
import play.api.mvc.Controller

object Application extends Controller {

  def index = Action {
    implicit request =>
      Ok(views.html.index(CMessage(ByURI,MsgEmpty)))
  }

  def about() = Action {
    implicit request =>
      Ok(views.html.about.about(CMessage(ByURI,MsgEmpty)))
  }
  
}