package es.weso.computex.entities

case class CIntegrityQuery (
    val query : CQuery, 
	val message : String,
	val errorMessages : List[CErrorMessage]
   ) {
  def size = errorMessages.size
}
