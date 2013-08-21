package es.weso.computex.entities

import java.io.ByteArrayInputStream
import java.io.InputStream
import scala.Array.canBuildFrom
import org.apache.commons.io.IOUtils
import es.weso.utils.JenaUtils
import java.io.IOException

case class CMessage(val action: String) {
  var message: String = CMessage.MsgOK
  var content: String = null
  var contentFormat: String = JenaUtils.TTL

  var verbose = false
  var ss = false
  var expand = false

  private var _contentIS: String = null
  private var _integrityQueries: Array[(String, IntegrityQuery)] = Array.empty

  def contentIS_=(is: InputStream): Unit = _contentIS = IOUtils.toString(is, "UTF-8")

  @throws(classOf[IOException])
  def contentIS: InputStream = {
    if (_contentIS == null) null
    else new ByteArrayInputStream(_contentIS.getBytes("UTF-8"))
  }

  def integrityQueries_=(iq: Array[(String, IntegrityQuery)]): Unit = _integrityQueries = iq
  def integrityQueries: Array[(String, IntegrityQuery)] = _integrityQueries.sortWith(_._1 < _._1)

  def size = _integrityQueries.map(_._2.size).foldLeft(0)(_ + _)

  def status = message match {
    case CMessage.MsgOK => CMessage.Valid
    case CMessage.MsgEmpty => CMessage.Idle
    case _ => CMessage.Invalid
  }
}

object CMessage {
  val Uri = "URI"
  val DirectInput = "DIRECT_IMPUT"
  val File = "FILE"

  val Valid = "valid"
  val Invalid = "invalid"
  val Idle = "idle"

  val MsgEmpty = "EMPTY"
  val MsgOK = "This document was successfully checked"
  val Msg404 = "File Not Found"
  val MsgBadFormed = "Bad Formed File"
  val MsgError = "Errors found while checking this document"
}