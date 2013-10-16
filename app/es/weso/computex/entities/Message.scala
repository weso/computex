package es.weso.computex.entities

import es.weso.computex.profile.Validator
import com.hp.hpl.jena.rdf.model.Model
import es.weso.computex.profile.Passed
import java.io.BufferedReader
import java.nio.charset.Charset
import java.io.InputStreamReader
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import es.weso.computex.profile.NotPassed
import es.weso.computex.profile.Profile
import java.io.InputStream
import es.weso.computex.Parser
import java.nio.charset.CodingErrorAction

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