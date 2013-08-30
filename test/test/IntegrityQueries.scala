package test

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import play.api.test.FakeApplication
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.contentType
import play.api.test.Helpers.route
import play.api.test.Helpers.running
import play.api.test.Helpers.status
import play.api.test.Helpers.OK
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import org.specs2.runner.JUnitRunner
import com.typesafe.config.Config
import es.weso.computex.Computex
import com.typesafe.config.ConfigFactory
import es.weso.computex.Computex
import org.specs2.specification.BeforeExample
import es.weso.computex.entities.CMessage
import java.io.ByteArrayInputStream
import org.apache.commons.io.FileUtils
import java.io.File
import es.weso.computex.entities.CIntegrityQuery
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class IntegrityQueries extends Specification with BeforeExample {

  var computex: Computex = null

  def before = {
    val conf: Config = ConfigFactory.load()
    val validationDir = conf.getString("validationDir")
    val computationDir = conf.getString("computationDir")
    val ontologyURI = conf.getString("ontologyURI")
    val closureFile = conf.getString("closureFile")
    val flattenFile = conf.getString("flattenFile")
    val findStepsQuery = conf.getString("findStepsQuery")

    this.computex = Computex(ontologyURI, validationDir, computationDir, closureFile, flattenFile, findStepsQuery)
  }

  def processFile(path: String): Map[String, CIntegrityQuery] = {
    val message = new CMessage(CMessage.File)
    message.contentIS = Computex.loadFile(s"file:${path}")
    before
    val iqs:Array[CIntegrityQuery] = computex.computex(message)
    val filteredIqs: Array[CIntegrityQuery] = iqs.filter(_.size > 0)
    filteredIqs.map(a => a.query.name -> a).toMap
  }

  "Processing 'badCopy.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badCopy.ttl")

    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("4", null)

    "error message should be 'Copy value does not match'" in {
      println(iq.message)
      iq.message must equalTo("Copy value does not match")
    }

    "must be comprised by only one error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head

    " must have '4' parameters" in {
      cm.params must size(4)
    }

  }
  
  "Processing 'goodCopy.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/goodCopy.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }
 
  "Processing 'goodIncremented.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/goodIncremented.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }
    
  "Processing 'goodMean.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/goodMean.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }
      
  "Processing 'goodNormalizedHigh.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/goodNormalizedHigh.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }
        
  "Processing 'goodNormalizedLow.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/goodNormalizedLow.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }
          
  "Processing 'goodRanking.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/goodRanking.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }

  "Processing 'goodWeightedMean.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/goodWeightedMean.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }

}