package es.weso.computex.entities

import java.io.ByteArrayOutputStream

import com.hp.hpl.jena.rdf.model.Model

import es.weso.utils.JenaUtils._
import es.weso.computex.PREFIXES

case class CModel(
    val model: Model, 
    val format:String = Turtle) {
  
  override def toString(): String = {
    val out = new ByteArrayOutputStream()
    model.setNsPrefixes(PREFIXES.cexMapping)
    model.write(out, format)
    out.toString().trim
  }
}