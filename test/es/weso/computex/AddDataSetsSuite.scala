package es.weso.computex

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import es.weso.computex.profile.Profile
import es.weso.computex.profile.Passed
import es.weso.computex.profile.NotPassed
import es.weso.utils.JenaUtils

class AddDataSetsSuite extends FunSpec 
	with ShouldMatchers 
	with SparqlSuite {

  val conf : Config = ConfigFactory.load()
  val demoMinUri = conf.getString("demoMinURI")
  
  
  describe("AddDatasets") {
   it("Should add 6 imputed datasets") {
     val m = JenaUtils.parseFromURI(demoMinUri, "", "TURTLE")
     val datasets = AddDatasets.addDatasets(m)
     val ds = JenaUtils.getValuesOfType(PREFIXES.qb_DataSet, datasets)
     ds.size should be(6)
   }

   it("Should compute imputed datasets") {
     val m = JenaUtils.parseFromURI(demoMinUri, "", "TURTLE")
     val datasets = AddDatasets.addDatasets(m)
     m.add(datasets)
     
     val p = Profile.Computex
     val computed = p.compute(m)
   }
}

}