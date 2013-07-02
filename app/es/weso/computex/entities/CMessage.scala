package es.weso.computex.entities

import java.io.InputStream

case class CMessage(val action : String) {
  var message: String = CMessage.VALID
  var status: String = CMessage.MSG_OK
  var content: String = null
  var contentIS : InputStream = null
  var errorMessages: List[(String, String)] = List.empty

  def size = errorMessages.size
  
}

object CMessage {
  val URI = "URI"
  val DIRECT_INPUT = "DIRECT_IMPUT"
  
  val VALID = "valid"
  val INVALID = "invalid"
  
  val MSG_ENPTY = "ENPTY"
  val MSG_OK = "This document was successfully checked"
  val MSG_404 = "File Not Found"
  val MSG_BAD_FORMED = "Bad Formed File"
  val MSG_ERROR = "Errors found while checking this document"
}