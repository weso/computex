package es.weso.computex

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Selector
import com.hp.hpl.jena.rdf.model.SimpleSelector
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.Property
import es.weso.utils.JenaUtils
import com.hp.hpl.jena.update.UpdateFactory
import java.net.URI
import scala.io.Source

case class ProfileParser(profile : Model) {

  val cex_integrityQuery = profile.createProperty(PREFIXES.cex + "integrityQuery")
  val cex_expandQuery = profile.createProperty(PREFIXES.cex + "expandQuery")
  val cex_name = profile.createProperty(PREFIXES.cex + "name")
  val cex_uri  = profile.createProperty(PREFIXES.cex + "uri")
  
  def validators : Seq[Validator] = {
    val vals = Vector.newBuilder[Validator]
    val iter = profile.listStatements(null,cex_integrityQuery,null) 
    while (iter.hasNext) {
      val s = iter.next
      val r = s.getObject()
      val name = JenaUtils.getLiteral(r,cex_name)
      val uri  = JenaUtils.getUri(r,cex_uri)
      val query = QueryFactory.read(uri)
      vals += Validator(query,name,uri)
    }
    vals.result
  }

  def expanders : Seq[Expander] = {
    val seq = Vector.newBuilder[Expander]
    val iter = profile.listStatements(null,cex_expandQuery,null) 
    while (iter.hasNext) {
      val s = iter.next
      val r = s.getObject()
      val name = JenaUtils.getLiteral(r,cex_name)
      val uri  = JenaUtils.getUri(r,cex_uri)
      val contents = Source.fromURI(new URI(uri)).mkString
      val updateReq  = UpdateFactory.create(contents)
      seq += Expander(updateReq,name,uri)
    }
    seq.result
  }

}
