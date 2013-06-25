package controllers

import play.api._
import play.api.mvc._
import com.typesafe.config._
import es.weso.computex.Computex

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def computex = Action {
    val conf: Config = ConfigFactory.load()
    val validationDir = conf.getString("validationDir")
    val ontologyURI = conf.getString("ontologyURI")
    val indexDataURI = "file:example.ttl"//conf.getString("indexDataURI")
    Ok(views.html.index("Your new application is ready."))
    val cex = new Computex
    cex.computex(indexDataURI, ontologyURI, validationDir)
    Ok(views.html.index("Your new application is ready."))
  }

}