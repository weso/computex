package es.weso.computex.profile

import java.net.URI
import scala.io.Source
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.RDFList
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.Resource
import es.weso.computex.profile._
import es.weso.computex.PREFIXES
import es.weso.utils.JenaUtils
import com.hp.hpl.jena.vocabulary.RDF
import com.hp.hpl.jena.update.UpdateFactory
import scala.collection.immutable.VectorBuilder

case class ProfileParser(profile : Model) {

  val rdf_type 				= profile.createProperty(PREFIXES.rdf + "type")
  val cex_ValidationProfile = profile.createProperty(PREFIXES.cex + "ValidationProfile")
  val cex_ontologyBase		= profile.createProperty(PREFIXES.cex + "ontologyBase")
  val cex_import 			= profile.createProperty(PREFIXES.cex + "import")
  val cex_integrityQuery 	= profile.createProperty(PREFIXES.cex + "integrityQuery")
  val cex_expandQuery 		= profile.createProperty(PREFIXES.cex + "expandQuery")
  val cex_expandSteps 		= profile.createProperty(PREFIXES.cex + "expandSteps")
  val cex_computeSteps 		= profile.createProperty(PREFIXES.cex + "computeSteps")
  val cex_name 				= profile.createProperty(PREFIXES.cex + "name")
  val cex_uri  				= profile.createProperty(PREFIXES.cex + "uri")

  def validators(resource: Resource): Seq[Validator] = {
    val vals = Vector.newBuilder[Validator]
    val iter = profile.listStatements(resource,cex_integrityQuery,null) 
    while (iter.hasNext) {
      val s = iter.next
      val r = s.getObject()
      val name = JenaUtils.getLiteral(r,cex_name)
      val uri  = JenaUtils.getObjectURI(r,cex_uri)
      val query = QueryFactory.read(uri.toString)
      vals += Validator(query,name,uri)
    }
    vals.result
  }

  def computeSteps(resource : Resource): Seq[ComputeStep] = {
    val seq = Vector.newBuilder[ComputeStep]
    val iter = profile.listStatements(resource,cex_computeSteps,null) 
    if (iter.hasNext) {
      val s = iter.next
      val nodeList : RDFNode = s.getObject()
      var current : Resource = nodeList.asResource()
      while (!RDF.nil.equals(current)) {
        val r = current.getRequiredProperty(RDF.first).getObject.asResource
        val name = JenaUtils.getLiteral(r,cex_name)
        val uri  = JenaUtils.getObjectURI(r,cex_uri)
        val contents = Source.fromURI(uri).mkString
        val query = QueryFactory.read(uri.toString)
        seq += ComputeStep(query,name,uri)
        current = current.getRequiredProperty(RDF.rest).getObject.asResource
      }
    } 
    seq.result
  }

  
  def expanders(resource : Resource): Seq[Expander] = {
    val seq = Vector.newBuilder[Expander]
    val iter = profile.listStatements(resource,cex_expandSteps,null) 
    if (iter.hasNext) {
      val s = iter.next
      val nodeList : RDFNode = s.getObject()
      var current : Resource = nodeList.asResource()
      while (!RDF.nil.equals(current)) {
        val r = current.getRequiredProperty(RDF.first).getObject.asResource
        val name = JenaUtils.getLiteral(r,cex_name)
        val uri  = JenaUtils.getObjectURI(r,cex_uri)
        val contents = Source.fromURI(uri).mkString
        val updateReq  = UpdateFactory.create(contents)
        seq += Expander(updateReq,name,uri)
        current = current.getRequiredProperty(RDF.rest).getObject.asResource
      }
    } 
    seq.result
  }

  def name(resource: Resource): String = {
    JenaUtils.getLiteral(resource, cex_name)
  }

  def base(resource: Resource): URI = {
    JenaUtils.getObjectURI(resource, cex_ontologyBase)
  }

  def uri(resource: Resource): String = {
    resource.getURI
  }

  def imports(resource: Resource, visited: Seq[URI]): Seq[(URI,Profile)] = {
    val seq = Vector.newBuilder[(URI,Profile)]
    val iter = profile.listStatements(resource,cex_import,null) 
    while (iter.hasNext) {
      val s = iter.next
      val uri = JenaUtils.getURI(s.getObject)
      if (visited.contains(uri)) {
        throw new Exception("imports: URI " + uri + " already visited. List of visited uris = " + visited)
      } else {
        val contents = Source.fromURI(uri).mkString
        val model = JenaUtils.parseFromString(contents) 
        val profiles = ProfileParser.fromModel(model,uri +: visited) 
        seq ++= profiles.map(p => (uri,p))
      }
    }
    seq.result
  }
  
  def getProfiles(visited: Seq[URI] = Seq()): Seq[Profile] = {
    val seq = Vector.newBuilder[Profile]
    val iter = profile.listStatements(null,rdf_type,cex_ValidationProfile) 
    while (iter.hasNext) {
      val s 	= iter.next
      val r 	= s.getSubject()
      val n    	= name(r)
      val u    	= uri(r)
      val b 	= base(r)
      val vals 	= validators(r)
      val exps 	= expanders(r)
      val comps = computeSteps(r)
      val imps 	= imports(r, new URI(r.getURI) +: visited)
      seq += Profile(b,vals,exps,comps,imps,n,u)
    }
    seq.result
  }
}

object ProfileParser {
  
  /**
   * Retrieves the list of profiles that appear in a Model
   * @param model the model to parse
   * @param visited the sequence of URIs that have already been visited 
   * 		Empty by default
   * 
   */
  def fromModel(model:Model,
      visited: Seq[URI] = Seq()
  ) : Seq[Profile] = ProfileParser(model).getProfiles(visited)

  /**
   * Generates a model from a profile
   */
  def toModel(profile: Profile) : Model = {
    val m= ModelFactory.createDefaultModel
    m.setNsPrefixes(PREFIXES.cexMapping)
    val root = m.createResource(profile.uri)
    val rdf_type 			 	= m.createProperty(PREFIXES.rdf + "type")
    val rdf_List 			 	= m.createProperty(PREFIXES.rdf + "List")
    val rdf_first 			 	= m.createProperty(PREFIXES.rdf + "first")
    val rdf_rest 			 	= m.createProperty(PREFIXES.rdf + "rest")
   	val cex_ontologyBase	 	= m.createProperty(PREFIXES.cex + "ontologyBase")
   	val cex_ValidationProfile 	= m.createProperty(PREFIXES.cex + "ValidationProfile")
   	val cex_import 				= m.createProperty(PREFIXES.cex + "import")
    val cex_integrityQuery 		= m.createProperty(PREFIXES.cex + "integrityQuery")
  	val cex_expandQuery 		= m.createProperty(PREFIXES.cex + "expandQuery")
  	val cex_expandSteps 		= m.createProperty(PREFIXES.cex + "expandSteps")
  	val cex_computeSteps 		= m.createProperty(PREFIXES.cex + "computeSteps")
  	val cex_name 				= m.createProperty(PREFIXES.cex + "name")
  	val cex_uri  				= m.createProperty(PREFIXES.cex + "uri")
  	
    m.add(root,rdf_type,cex_ValidationProfile)
    m.add(root,cex_name,profile.name)
    val uriBase = m.createResource(profile.ontologyBase.toString)
    m.add(root,cex_ontologyBase,uriBase)
    for (i <- profile.imports) {
      val uri = m.createResource(i._1.toString)
      m.add(root,cex_import,uri)
    }

    if (profile.expanders.length > 0) {
     // Generate RDF Collection list of expanders
     val lsNodes = Vector.newBuilder[RDFNode]    
     for (e <- profile.expanders) {
       val currentNode = m.createResource
       m.add(currentNode,cex_name,e.name)
       val uriExpander = m.createResource(e.uri.toString)
       m.add(currentNode,cex_uri,uriExpander)
       lsNodes += currentNode
     }
     val listExpanders = m.createList(lsNodes.result.toArray)
     m.add(root,cex_expandSteps,listExpanders)
    }
    
    if (profile.computeSteps.length > 0) {
    // Generate RDF Collection list of computeSteps
    val lsComps = Vector.newBuilder[RDFNode]    
    for (e <- profile.computeSteps) {
      val currentNode = m.createResource
      m.add(currentNode,cex_name,e.name)
      val uriComputeStep = m.createResource(e.uri.toString)
      m.add(currentNode,cex_uri,uriComputeStep)
      lsComps += currentNode
    }
    val listComputeStep = m.createList(lsComps.result.toArray)
    m.add(root,cex_computeSteps,listComputeStep)
    }
    for (v <- profile.validators) {
      val resourceValidator = m.createResource
      m.add(root,cex_integrityQuery,resourceValidator)
      m.add(resourceValidator,cex_name,v.name)
      val uriValidator = m.createResource(v.uri.toString)
      m.add(resourceValidator,cex_uri,uriValidator)
    }
    m
  }
  
}