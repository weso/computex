package es.weso.computex.entities

import com.hp.hpl.jena.rdf.model.Model
import java.io.ByteArrayOutputStream
import es.weso.utils.JenaUtils._

case class ErrorMessage(val message: String, val model: CModel) {

  def this(message:String, model:Model) = this(message, CModel(model))
  
}