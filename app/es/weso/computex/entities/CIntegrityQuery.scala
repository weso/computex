package es.weso.computex.entities

import com.hp.hpl.jena.query.Query

case class IntegrityQuery (val query : (String, Query)) {
	var message : String = null
	var errorMessages : List[CErrorMessage] = null
	
	def size = errorMessages.size
}
