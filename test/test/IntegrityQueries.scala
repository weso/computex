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

// TODO: The following suite should be restored...

/*
 
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
    val message = new CMessage(CMessage.File,"test")
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
  
    "Processing 'badCopyNoSource.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badCopyNoSource.ttl")

    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("5", null)

    "error message should be 'Observation does not have value in copy'" in {
      println(iq.message)
      iq.message must equalTo("Observation does not have value in copy")
    }

    "must be comprised by only one error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head

    " must have '2' parameters" in {
      cm.params must size(2)
    }

  }

  "Processing 'badIncremented.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badIncremented.ttl")

    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("2", null)

    "error message should be 'Adjusted value does not match'" in {
      println(iq.message)
      iq.message must equalTo("Adjusted value does not match")
    }

    "must be comprised by only one error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head

    " must have '4' parameters" in {
      cm.params must size(4)
    }

  }

  "Processing 'badIncrementedNoValueSource.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badIncrementedNoValueSource.ttl")

    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("1", null)

    "error message should be 'Source observation does not have value in adjusted computation'" in {
      println(iq.message)
      iq.message must equalTo("Source observation does not have value in adjusted computation")
    }

    "must be comprised by only one error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head

    " must have '2' parameters" in {
      cm.params must size(2)
    }

  }

  "Processing 'badMean.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badMean.ttl")

    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("6", null)

    "error message should be 'Mean value does not match'" in {
      println(iq.message)
      iq.message must equalTo("Mean value does not match")
    }

    "must be comprised by only one error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head

    " must have '3' parameters" in {
      cm.params must size(3)
    }

  }

  "Processing 'badMeanNoSource.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badMeanNoSource.ttl")

    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("6", null)

    "error message should be 'Observation does not have value in mean'" in {
      println(iq.message)
      iq.message must equalTo("Observation does not have value in mean")
    }

    "must be comprised by only one error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head

    " must have '2' parameters" in {
      cm.params must size(2)
    }

  }

  "Processing 'badNormalizedLow.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badNormalizedLow.ttl")

    "must fail only two integrity queries" in {
      map must size(2)
    }

    val iq8: CIntegrityQuery = map.getOrElse("8", null)

    "error message should be 'Normalized value does not match computed z-score'" in {
      println(iq8.message)
      iq8.message must equalTo("Normalized value does not match computed z-score")
    }

    "must be comprised by only one error message" in {
      iq8.errorMessages must size(1)
    }

    val cm8 = iq8.errorMessages.head

    " must have '4' parameters" in {
      cm8.params must size(4)
    }
    
    " 'highLow' must be 'http://purl.org/weso/ontology/computex#Low'" in {
    	cm8.params.filter(_.name == "highLow").head.value must equalTo("http://purl.org/weso/ontology/computex#Low")
    }

    val iq6a: CIntegrityQuery = map.getOrElse("9a", null)

    "error message should be 'Every Slice must have exactly one sliceStructure'" in {
      println(iq6a.message)
      iq6a.message must equalTo("Every Slice must have exactly one sliceStructure")
    }

    "must be comprised by only one error message" in {
      iq6a.errorMessages must size(1)
    }

    val cm6a = iq6a.errorMessages.head

    " must have '1' parameters" in {
      cm6a.params must size(1)
    }

  }

  "Processing 'badNormalizedHigh.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badNormalizedHigh.ttl")

    "must fail only two integrity queries" in {
      map must size(2)
    }

    val iq8: CIntegrityQuery = map.getOrElse("8", null)

    "error message should be 'Normalized value does not match computed z-score'" in {
      println(iq8.message)
      iq8.message must equalTo("Normalized value does not match computed z-score")
    }

    "must be comprised by only one error message" in {
      iq8.errorMessages must size(1)
    }

    val cm8 = iq8.errorMessages.head

    " must have '4' parameters" in {
      cm8.params must size(4)
    }
    
    " 'highLow' must be 'http://purl.org/weso/ontology/computex#High'" in {
    	cm8.params.filter(_.name == "highLow").head.value must equalTo("http://purl.org/weso/ontology/computex#High")
    }

    val iq6a: CIntegrityQuery = map.getOrElse("9a", null)

    "error message should be 'Every Slice must have exactly one sliceStructure'" in {
      println(iq6a.message)
      iq6a.message must equalTo("Every Slice must have exactly one sliceStructure")
    }

    "must be comprised by only one error message" in {
      iq6a.errorMessages must size(1)
    }

    val cm6a = iq6a.errorMessages.head

    " must have '1' parameters" in {
      cm6a.params must size(1)
    }

  }
  
  "Processing 'badRanking.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/badRanking.ttl")

    "must fail only one integrity query" in {
      map must size(1)
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
    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("7", null)

    "error message should be 'Observation with Status obsStatus-M (Missing) should not have value'" in {
      println(iq.message)
      iq.message must equalTo("Observation with Status obsStatus-M (Missing) should not have value")
    }

    "must be comprised by only two error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head
    " must have '1' parameters" in {
      cm.params must size(2)
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

  "Processing 'obs_noSheet-type.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/obs_noSheet-type.ttl")
    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("9", null)

    "error message should be 'Observation does not have sheet-type'" in {
      println(iq.message)
      iq.message must equalTo("Observation does not have sheet-type")
    }

    "must be comprised by only two error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head
    " must have '1' parameters" in {
      cm.params must size(1)
    }
  }
  "Processing 'obs_noValue.ttl' file " should {
    val map: Map[String, CIntegrityQuery] = processFile("src/test/resources/obs_noValue.ttl")
    "must fail only one integrity query" in {
      map must size(1)
    }

    val iq: CIntegrityQuery = map.getOrElse("11", null)

    "error message should be 'Observation does not have value'" in {
      println(iq.message)
      iq.message must equalTo("Observation does not have value")
    }

    "must be comprised by only two error message" in {
      iq.errorMessages must size(1)
    }

    val cm = iq.errorMessages.head
    " must have '1' parameters" in {
      cm.params must size(1)
    }
  }
}

*/