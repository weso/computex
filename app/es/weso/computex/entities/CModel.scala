package es.weso.computex.entities

import com.hp.hpl.jena.rdf.model.Model
import java.io.ByteArrayOutputStream
import es.weso.utils.JenaUtils._

case class CModel(val model: Model) {
  var format = TURTLE
  override def toString(): String = {
    val out = new ByteArrayOutputStream()
    model.write(out, format)
    out.toString().trim
  }
}