package controllers

import play.api._
import play.api.mvc._
import com.typesafe.config._
import es.weso.computex.Parser
import es.weso.computex.Computex
import es.weso.utils.JenaUtils.TURTLE

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def computex = Action {
    val conf: Config = ConfigFactory.load()
    val validationDir = conf.getString("validationDir")
    val ontologyURI = conf.getString("ontologyURI")
    val indexDataURI = "file:example.ttl"//conf.getString("indexDataURI")
    
    val cex = new Computex
    
    val model = cex.computex(indexDataURI, ontologyURI, validationDir)
    
    for(i<-Parser.parseErrors(model)){
      println(i.message)
      i.model.write(System.out,TURTLE)
    }
    
    Ok(views.html.index("Your new application is ready."))
  }

}