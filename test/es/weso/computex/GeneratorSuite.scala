import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class GeneratorSuite 
	extends FunSpec 
	with ShouldMatchers {

describe("Generator") {
  it("should generate a example with one indicator and one country") {
    List().length should be(0)
  }
}

}