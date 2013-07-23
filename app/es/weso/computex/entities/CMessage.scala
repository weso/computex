package es.weso.computex.entities

import java.io.InputStream
import es.weso.utils.JenaUtils
import java.io.ByteArrayInputStream
import scala.io.Source._
import org.apache.commons.io.IOUtils

case class CMessage(val action : String) {
  var message: String = CMessage.MSG_OK
  var content: String = null
  var contentFormat : String = JenaUtils.TTL
  
  var verbose = false
  var ss = false
  var expand = false
  
  private var _contentIS : String = null
  private var _integrityQueries: Array[(String, IntegrityQuery)] = Array.empty
  
  def contentIS_= (is: InputStream):Unit = _contentIS= IOUtils.toString(is, "UTF-8")
  def contentIS : InputStream = new ByteArrayInputStream(_contentIS.getBytes("UTF-8"))
  
  def integrityQueries_= (iq:Array[(String, IntegrityQuery)]):Unit = _integrityQueries = iq
  def integrityQueries : Array[(String, IntegrityQuery)]= _integrityQueries.sortWith(_._1<_._1)
  
  def size = _integrityQueries.map(_._2.size).foldLeft(0)(_+_)
  
  def status = message match {
    case CMessage.MSG_OK | CMessage.MSG_EMPTY => CMessage.VALID
    case _ => CMessage.INVALID
  }
}

object CMessage {
  val URI = "URI"
  val DIRECT_INPUT = "DIRECT_IMPUT"
  val FILE = "FILE"
    
  val VALID = "valid"
  val INVALID = "invalid"
  
  val MSG_EMPTY = "EMPTY"
  val MSG_OK = "This document was successfully checked"
  val MSG_404 = "File Not Found"
  val MSG_BAD_FORMED = "Bad Formed File"
  val MSG_ERROR = "Errors found while checking this document"
}