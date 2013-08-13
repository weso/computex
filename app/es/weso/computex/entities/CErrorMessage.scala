package es.weso.computex.entities

import com.hp.hpl.jena.rdf.model.Model

case class CErrorMessage(val message: String, val params : Array[CParam], val model: CModel) {

  def this(message:String, params : Array[CParam], model:Model) = this(message, params, new CModel(model))
  
}