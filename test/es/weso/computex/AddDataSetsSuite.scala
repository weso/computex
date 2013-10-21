package es.weso.computex

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import es.weso.computex.profile.Profile
import es.weso.computex.profile.Passed
import es.weso.computex.profile.NotPassed
import es.weso.utils.JenaUtils._

class AddDataSetsSuite extends FunSpec 
	with ShouldMatchers 
	with SparqlSuite {

  val conf : Config = ConfigFactory.load()
  val demoMinUri = conf.getString("demoMinURI")
  
  
  describe("AddDatasets") {
   it("Should add 6 imputed datasets") {
     val m = parseFromURI(demoMinUri, "", "TURTLE")
     val datasets = AddDatasets.addDatasets(m)
     val ds = getValuesOfType(PREFIXES.qb_DataSet, datasets)
     ds.size should be(6)
   }

   it("Should compute imputed datasets and copy values") {
     val m = parseFromURI(demoMinUri, "", "TURTLE")
     val datasets = AddDatasets.addDatasets(m)
     m.add(datasets)
     
     val p = Profile.Computex
     val (expanded,computed) = p.compute(m)
     val ds = getValuesOfType(PREFIXES.cex_Copy, expanded)
     ds.size should be(49)
   }

   it("Should compute imputed datasets and calculate mean between values") {
     val m = parseFromURI(demoMinUri, "", "TURTLE")
     val datasets = AddDatasets.addDatasets(m)
     m.add(datasets)
     
     val p = Profile.Computex
     val (expanded,computed) = p.compute(m)
     val ds = getValuesOfType(PREFIXES.cex_Mean, expanded)
     ds.size should be(6)
     model2File(expanded,"generated.ttl")
   }

   it("Should compute imputed datasets and calculate average growth") {
     val m = parseFromURI(demoMinUri, "", "TURTLE")
     val datasets = AddDatasets.addDatasets(m)
     m.add(datasets)
     
     val p = Profile.Computex
     val (expanded,computed) = p.compute(m)
     val ds = getValuesOfType(PREFIXES.cex_AverageGrowth, expanded)
     ds.size should be(1)
     model2File(expanded,"generated.ttl")
   }

  }

}