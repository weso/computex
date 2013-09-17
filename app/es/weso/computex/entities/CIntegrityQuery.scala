package es.weso.computex.entities

case class CIntegrityQuery (
    val query : CQuery, 
	val message : String,
	val errorMessages : List[ErrorMessage]
   ) {
  def size = errorMessages.size
}
