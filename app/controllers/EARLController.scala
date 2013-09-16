package controllers

import scala.collection.JavaConversions._
import es.weso.computex.entities.CMessage
import play.api.mvc.Action
import play.api.mvc.Controller
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.weso.utils.JenaUtils
import java.io.File
import play.api.mvc.Accepting
import play.api.mvc.SimpleResult
import play.api.mvc.RequestHeader
import play.api.mvc.RawBuffer
import es.weso.computex.entities.ByFile
import es.weso.computex.entities.MsgOk

object EARLController extends Controller {

  val PlainText = Accepting("text/plain")
  val Turtle = Accepting("text/turtle")
  val XTurtle = Accepting("application/x-turtle")
  
  def getEARL(name: String) = Action(parse.anyContent) {
    implicit request =>
      println(request.accept)
      render {
        case PlainText() => downloadEARL(name)
        case Turtle() => downloadEARL(name)
        case XTurtle() => downloadEARL(name)
        case _ => displayEARL(name)
      }
  }

  private def downloadEARL(name: String): SimpleResult[Array[Byte]] = {
    Ok.sendFile(content = new File(s"public/earls/${name}"), fileName = _ => name)
  }

  private def displayEARL(name: String)(implicit request: RequestHeader): SimpleResult[play.api.templates.Html] = {
    val model = ModelFactory.createDefaultModel
    model.read(s"file:public/earls/${name}", "", JenaUtils.TTL)
    val message = CMessage(ByFile,MsgOk,Some(name))
    Ok(views.html.earl.defaultEARL(model, message))
  }
}