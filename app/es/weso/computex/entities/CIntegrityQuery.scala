package es.weso.computex.entities

case class CIntegrityQuery (val query : CQuery) {
  
	var message : String = null
	var errorMessages : List[CErrorMessage] = null
	
	def size = errorMessages.size
}
