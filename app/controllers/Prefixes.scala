package controllers

import es.weso.computex.entities._
import es.weso.computex.entities.Action._
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.computex.PREFIXES

object Prefixes extends Controller {
  
  def sparql = Action {
    Ok(PREFIXES.prefixTemplateSparql)
  }

  def turtle = Action {
    Ok(PREFIXES.prefixTemplateTurtle)
  }

}