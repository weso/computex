package es.weso.computex.entities

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import scala.Array.canBuildFrom
import es.weso.utils.JenaUtils
import java.nio.charset.CodingErrorAction
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import play.api.Logger

// TODO...this case class contains a lot of vars that I think we could remove
case class CMessage(val action: String) {
  
  // TODO....try to remove the use of var's
  var message: 		 String = CMessage.MsgOK
  var content: 		 String = null
  var contentFormat: String = JenaUtils.TTL

  var verbose 	 = false
  var showSource = false
  var expand 	 = false

  // Try to remove the use of var's and null's !!!
  private var _contentIS: String = null
  private var _contentCharset: Charset = null
  private var _integrityQueries: Array[CIntegrityQuery] = Array.empty

  def integrityQueries_=(iq: Array[CIntegrityQuery]): Unit = 
    _integrityQueries = iq
  
  def integrityQueries: Array[CIntegrityQuery] = 
    _integrityQueries.sortWith(_.query.name < _.query.name)

  def size = _integrityQueries.map(_.size).foldLeft(0)(_ + _)

  def status = message match {
    case CMessage.MsgOK => CMessage.Valid
    case CMessage.MsgEmpty => CMessage.Idle
    case _ => CMessage.Invalid
  }

  def contentIS_= (is: InputStream): Unit = {
    val charsetDecoder = Charset.forName("UTF-8").newDecoder();
    charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
    charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    _contentIS = {
      val in = new BufferedInputStream(is);
      val inputReader = new InputStreamReader(in, charsetDecoder);
      val bufferedReader = new BufferedReader(inputReader);
      val sb = new StringBuilder();
      var line = bufferedReader.readLine();
      while (line != null) {
        sb.append(line);
        line = bufferedReader.readLine();
      }
      bufferedReader.close();
      sb.toString();
    }
    _contentCharset = charsetDecoder.charset()
  }

  def contentIS: InputStream = {
    if (_contentIS == null) null
    else {
      new ByteArrayInputStream(_contentIS.getBytes(_contentCharset))
    }
  }
  
  def charset: Charset = _contentCharset
}

object CMessage {
  val Uri 			= "URI"
  val DirectInput 	= "DIRECT_IMPUT"
  val File 			= "FILE"

  val Valid 		= "valid"
  val Invalid 		= "invalid"
  val Idle 			= "idle"

  val MsgEmpty 		= "EMPTY"
  val MsgOK 		= "This document was successfully checked"
  val Msg404 		= "File Not Found"
  val MsgBadFormed 	= "Bad Formed File"
  val MsgError 		= "Errors found while checking this document"
}