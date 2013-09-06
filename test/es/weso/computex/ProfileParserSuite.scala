package es.weso.computex

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config._
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.util.FileManager
import scala.collection.JavaConversions._
import com.hp.hpl.jena.vocabulary.RDF
import java.io.FileOutputStream
import java.io.File
import java.io.StringWriter
import es.weso.utils.JenaUtils._

class ProfileParserSuite 
	extends FunSpec 
	with ShouldMatchers {
 describe("Profile Parser") {
   it("Should obtain a list of validators for RDF Data Cube") {
     val contents = scala.io.Source.fromFile("ontology/cube.ttl").mkString;
     val cube = parseModel(contents).get
     val profileParser = ProfileParser(cube)
     val validators = profileParser.validators
     validators.length should be(23)
   }

   it("Should obtain a list of validators for Computex") {
     val contents = scala.io.Source.fromFile("ontology/computex.ttl").mkString;
     val cube = parseModel(contents).get
     val profileParser = ProfileParser(cube)
     val validators = profileParser.validators
     validators.length should be(1)
   }

   it("Should obtain a list of expanders for RDF Data Cube profile") {
     val contents = scala.io.Source.fromFile("ontology/cube.ttl").mkString;
     val cube = parseModel(contents).get
     val profileParser = ProfileParser(cube)
     val validators = profileParser.expanders
     validators.length should be(2)
   }

 
   it("Should obtain a list of expanders for Computex profile") {
     val contents = scala.io.Source.fromFile("ontology/computex.ttl").mkString;
     val cube = parseModel(contents).get
     val profileParser = ProfileParser(cube)
     val validators = profileParser.expanders
     validators.length should be(1)
   }
}
}
