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
    val iqs: Array[CIntegrityQuery] = computex.computex(message)
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
  
  "Processing 'badRanking.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badRanking.ttl")

    "must fail only two integrity query" in {
      map must size(2)
    }

    val iq11: CIntegrityQuery = map.getOrElse("11", null)

    "error message should be 'Observation does not have value'" in {
      println(iq11.message)
      iq11.message must equalTo("Observation does not have value")
    }

    "must be comprised by only two error message" in {
      iq11.errorMessages must size(2)
    }

    for (em <- iq11.errorMessages) {
      " must have '1' parameters" in {
        em.params must size(1)
      }
    }

    val iq12: CIntegrityQuery = map.getOrElse("12", null)

    "error message should be 'Ranking value does not match'" in {
      println(iq12.message)
      iq12.message must equalTo("Ranking value does not match")
    }

    "must be comprised by only one error message" in {
      iq12.errorMessages must size(1)
    }

    val cm12 = iq12.errorMessages.head

    " must have '5' parameters" in {
      cm12.params must size(5)
    }

  }

  "Processing 'badWeightedMean.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badWeightedMean.ttl")

    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("13", null)

    "error message should be 'Weighted Mean does not match'" in {
      println(iq.message)
      iq.message must equalTo("Weighted Mean does not match")
    }

    "must be comprised by only one error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head

    " must have '3' parameters" in {
      cm.params must size(3)
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

  "Processing 'missingObsNoValues.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/missingObsNoValues.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }

  "Processing 'obs_noSheet-type.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/obs_noSheet-type.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }

  "Processing 'obs_noValue.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/obs_noValue.ttl")
    "must pass all integrity queries" in {
      map must size(0)
    }
  }

  "Processing 'obs_2values.ttl' file " should {
    
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/obs_2values.ttl")
    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("10", null)

    "error message should be 'Observation has two different values'" in {
      println(iq.message)
      iq.message must equalTo("Observation has two different values")
    }

    "must be comprised by only two error message" in {
      iq.errorMessages must size(2)
    }

    for (em <- iq.errorMessages) {
      " must have '3' parameters" in {
        em.params must size(3)
      }
    }
  }
}