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
import es.weso.computex.profile.Profile
import com.hp.hpl.jena.rdf.model.Model
import es.weso.computex.profile._
import es.weso.computex.profile.VReport.VReport


sealed abstract class Action;
case object ByURI 			extends Action ;
case object ByDirectInput 	extends Action ;
case object ByFile 			extends Action ;

sealed abstract class Message ;
case object MsgOk 		extends Message {
  override def toString = "OK";
}

case object MsgEmpty 	extends Message {
  override def toString = "Empty";
}

case object MsgOK 		extends Message {
  override def toString = "This document was successfully checked"
}

case object Msg404 		extends Message {
  override def toString = "File Not Found"
}

case object MsgBadFormed extends Message {
  override def toString = "Bad Formed File"
}	

case class MsgError(msg: String) 	extends Message {
 override def toString = "Error: " + msg
}


sealed abstract class Status;
case object Valid 	extends Status;
case object Invalid 	extends Status;
case object Idle		extends Status;

case class CMessage(
    val action: 		Action,
    val message: 		Message,
    val content: 		Option[String] 		= None ,
    val profile: 		Option[Profile] 	= None,
    val contentFormat: 	String 				= JenaUtils.TTL,
    val verbose: 		Boolean 			= false,
    val showSource: 	Boolean 			= false,
    val expand: 		Boolean 			= false,
    val result:			Option[(VReport,Model)]		= None
    ) {

  def status = message match {
    case MsgOK 		=> Valid
    case MsgEmpty 	=> Idle
    case _ 			=> Invalid
  }

  
  /**
   * Content as InputStream
   */
  def contentIS : InputStream = {
    CMessage.str2is(content.getOrElse(""),Charset.forName("UTF-8"))
  }

  def setError(str: String) : CMessage = 
    this.copy(message=MsgError(str))
  
  def setResult(r: (VReport,Model) ): CMessage =
    this.copy(result = Some(r))
    
  def getModel : Option[Model] = {
    content.map(str => JenaUtils.parseModel(str, "", contentFormat))
  }
    
  def hasErrors : Boolean = {
    ???
  }

  def numErrors : Integer = {
    ???
  }

  def integrityQueries : List[CIntegrityQuery] = {
    ???
  }

}

object CMessage {
 
  /**
   * Generic utility to convert from String to InputStream
   */
  def str2is (str: String, charset: Charset) : InputStream = {
    new ByteArrayInputStream(str.getBytes(charset))
  }
 
    /**
   * Generic utility to convert from InputStream to String
   */
  def is2str (is: InputStream) : String = {
    val charsetDecoder = Charset.forName("UTF-8").newDecoder();
    charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
    charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
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

}