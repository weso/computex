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

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Computex V.S." should {

    "send 404 on a bad request" in {
      running(FakeApplication()) {
        route(FakeRequest(GET, "/foo")) must beNone
      }
    }

    "render the index page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get

        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain("id=\"uri\"")
      }
    }

    "render the uri page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/uri")).get

        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain("id=\"uri\"")
      }
    }

    "render the file upload page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/file")).get

        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain("id=\"uploaded_file\"")
      }
    }

    "render the direct input page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/direct")).get

        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain("id=\"fragment\"")
      }
    }
  }
}