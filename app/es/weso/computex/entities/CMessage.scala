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
import es.weso.computex.entities.Message._
import es.weso.computex.Parser


sealed abstract class Action;
case object ByURI 			extends Action ;
case object ByDirectInput 	extends Action ;
case object ByFile 			extends Action ;

sealed abstract class Message ;

case object MsgEmpty 	extends Message {
  override def toString = "Empty";
}

case class MsgPassed(
    val vals: Seq[Validator],
    val modelChecked: Model
    )  extends Message {
  override def toString = "This document was successfully checked"
}

case class MsgNotPassed(
    val valsPassed: Seq[Validator],
    val valsNotPassed: Seq[CIntegrityQuery],
    val modelChecked: Model
    )  extends Message {
  override def toString = "This document contains errors"
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
case object Valid 		extends Status;
case object Invalid 	extends Status;
case object Idle		extends Status;

case class Options(
    val profile:		Profile = Profile.Computex,
    val contentFormat:	String  = JenaUtils.TTL,
    val verbose: 		Boolean = false,
    val showSource:		Boolean = true,
    val expand:			Boolean = true,
 //   val imports:		Boolean = true,
    val prefix:			Boolean = true
    )

case class CMessage(
    val action: 		Action,
    val message: 		Message,
    val options: 		Options = Options()
    ) {
  
  def setError(str: String) : CMessage = 
    this.copy(message=MsgError(str))
  
  def setResult(r: VReport, m: Model): CMessage =
    r match {
    	case Passed(vs) => 
    	  this.copy(message = MsgPassed(vs,m))
    	case NotPassed((vs,nvs)) => 
    	  this.copy(message = MsgNotPassed(vs,parseErrors(nvs),m))
    }
  

   def status : Status = message match {
    case MsgPassed(_,_) => Valid
    case MsgEmpty 	 	=> Idle
    case _ 			 	=> Invalid
  }

 def passed = !hasErrors

 def hasErrors : Boolean = message match {
   case MsgNotPassed(_,_,_) => true
   case _ 					=> false
 }
 
 def numErrors: Int = message match {
   case MsgNotPassed(_,nvs,_) => nvs.size
   case _ => 0
 }
 
 def numPassed: Int = message match {
   case MsgNotPassed(vs,_,_) 	=> vs.size
   case MsgPassed(vs,_) 		=> vs.size
   case _ 						=> 0
 }
 
 def integrityQueries: Seq[CIntegrityQuery] = message match {
   case MsgNotPassed(_,iqs,_) => iqs 
   case _ => Seq()
 }
 
 def passedVals: Seq[Validator] = message match {
   case MsgNotPassed(vs,_,_) => vs
   case MsgPassed(vs,_) => vs
   case _ => Seq()
 }

 def profile 		= options.profile
 def contentFormat 	= options.contentFormat
 def showSource 	= options.showSource
 def expand	 		= options.expand
 def verbose 		= options.verbose
  
}

object Message {
 
  def parseErrors(errors : Seq[(Validator,Model)]) : Seq[CIntegrityQuery] = {
    errors.map((p) => 
      Parser.parse(CQuery(p._1.name,p._1.query),p._2))
  }

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

 
  def validate(p: Profile, m: Model, 
      expand: Boolean, 
      imports: Boolean = true) : Message = {
    val (report,checked) = p.validate(m,expand,imports) 
    report match {
      case Passed(vs) => MsgPassed(vs,checked)
      case NotPassed((vs,nvs)) => MsgNotPassed(vs,parseErrors(nvs),checked)
    }
  }
}