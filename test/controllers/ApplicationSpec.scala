package controllers

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import scala.actors.Future
import play.api.mvc.Controller
import play.api.mvc.SimpleResult
import play.api.mvc.Results

object ApplicationSpec extends Specification with Results {

  class TestApplication() extends Controller with Application

  "Index page#index" should {
    "should contain Computex" in {
      val controller = new TestApplication()
      val result = controller.index().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText must contain("Computex") 
    }
  }
}