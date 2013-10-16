package es.weso.computex.entities

import com.hp.hpl.jena.rdf.model.Model

case class ErrorMessage(
    val params : Array[CParam], 
    val model: CModel) {

  def this(
      params : Array[CParam], 
      model:Model) = this(params, CModel(model)
  )
  
}