package es.weso.computex.entities

import com.hp.hpl.jena.rdf.model.Model

case class ErrorMessage (val message:String, val model:Model) {

}