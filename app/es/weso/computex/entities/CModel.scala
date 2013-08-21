package es.weso.computex.entities

import java.io.ByteArrayOutputStream

import com.hp.hpl.jena.rdf.model.Model

import es.weso.utils.JenaUtils.Turtle

case class CModel(val model: Model, var format:String) {
  
  def this(model:Model) = this(model, Turtle)
  
  override def toString(): String = {
    val out = new ByteArrayOutputStream()
    model.setNsPrefix("cex", "http://purl.org/weso/ontology/computex#")
    model.setNsPrefix("qb", "http://purl.org/linked-data/cube#")
    model.setNsPrefix("obs", "http://data.webfoundation.org/webindex/v2013/observation/")
    model.setNsPrefix("indicator", "http://data.webfoundation.org/webindex/v2013/indicator/")
    model.setNsPrefix("wi-onto", "http://data.webfoundation.org/webindex/ontology/")
    model.setNsPrefix("country", "http://data.webfoundation.org/webindex/v2013/country/")
    model.setNsPrefix("organization", "http://data.webfoundation.org/webindex/v2013/organization")
    model.setNsPrefix("dataset", "http://data.webfoundation.org/webindex/v2013/dataset/")
    model.write(out, format)
    out.toString().trim
  }
}