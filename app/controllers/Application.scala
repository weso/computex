package controllers

import es.weso.computex.entities._
import es.weso.computex.entities.Action._
import play.api.mvc.Action
import play.api.mvc.Controller

// We use a explicitly typed self reference for Unit testing
// following Play documentation for testing
trait Application {
  this: Controller => 

  def index = Action {
    implicit request =>
      Ok(views.html.index(CMessage(ByURI,MsgEmpty)))
  }

  def about() = Action {
    implicit request =>
      Ok(views.html.about.about(CMessage(ByURI,MsgEmpty)))
  }
  
}
  
object Application extends Controller with Application