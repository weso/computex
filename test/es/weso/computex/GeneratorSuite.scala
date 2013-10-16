package es.weso.computex

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

class GeneratorSuite extends FunSpec 
	with ShouldMatchers 
	with SparqlSuite {

val conf : Config = ConfigFactory.load()

  val validationDir = conf.getString("validationDir")
  val computationDir = conf.getString("computationDir")
  val cubeDataDir = conf.getString("cubeDataDir")
  val ontologyURI  = conf.getString("ontologyURI")
  val demoURI = conf.getString("demoURI")
  val demoAbbrURI = conf.getString("demoAbbrURI")
  val demoCubeUri = conf.getString("demoCubeURI")
  val closureFile = conf.getString("closureFile")
  val flattenFile = conf.getString("flattenFile")
  val findStepsQuery 	= conf.getString("findStepsQuery") 
  
  
  val cex = Computex(ontologyURI,validationDir,computationDir,closureFile,flattenFile,findStepsQuery)

  describe("Generator") {
   describe("An example with one indicator, one country and one year should pass Cube integrity checks") {
	   val g = Generator(1,1,1)
	   val expanded = cex.expandCube(g.model)
	   passDir(expanded,cubeDataDir)
   }

   describe("An example with one indicator, one country and one year should pass Computex integrity checks") {
	   val g = Generator(1,1,1)
	   val expandedCube = cex.expandCube(g.model)
	   val expandedCex 	= cex.expandComputex(expandedCube)
	   passDir(expandedCex,validationDir)
   }

   describe("An example with two indicators, three countries and 4 years should pass Cube integrity checks") {
	   val g = Generator(2,3,4)
	   val expanded = cex.expandCube(g.model)
	   passDir(g.model,cubeDataDir)
   }

   ignore("An example with twenty indicators, 30 countries and 4 years should pass Cube integrity checks") {
	   val g = Generator(20,30,4)
	   val expanded = cex.expandCube(g.model)
	   passDir(g.model,cubeDataDir)
   }
}

}