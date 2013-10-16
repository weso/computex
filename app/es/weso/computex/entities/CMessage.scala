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
import es.weso.computex.entities.Action._
import es.weso.computex.Parser

case class CMessage(
    val action: 		Action,
    val message: 		Message,
    val options: 		Options = Options(),
    val content:		String = ""
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

 def modelAsString() : String = message match {
   case MsgNotPassed(_,_,m) => JenaUtils.model2Str(m)
   case MsgPassed(_,m) => JenaUtils.model2Str(m)
   case _ => ""
  }
 
 def profile 		= options.profile
 def contentFormat 	= options.contentFormat
 def showSource 	= options.showSource
 def expand	 		= options.expand
 def verbose 		= options.verbose
}
