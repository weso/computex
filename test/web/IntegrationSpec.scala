package web

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {
  
  "Web Application" should {
    
    "work within a browser" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        
        browser.waitUntil[Boolean]{
          browser.pageSource contains ("Computex")
          browser.$("title").first.getText.contains("Computex - Validator")
          browser.$("#title").first().getText().contains("COMPUTEX")
        }
      }
    }

    "visit about page" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        // Go to about
        browser.waitUntil[Boolean]{
         browser.$("#weso-link").click()
         browser.$("#about").first.getText.contains("About Computex")
        }
      }
    }

   "validate by input" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        // Go to input
        browser.waitUntil[Boolean]{
         browser.$("#by_direct a").click()
         browser.pageSource.contains("fragment")
        }
      }
    }

   "validate by file" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        // Go to file
        browser.waitUntil[Boolean]{
         browser.$("#by_file a").click()
         browser.pageSource.contains("File:")
        }
      }
    }
  }
  
}