package es.weso.computex

import com.hp.hpl.jena.rdf.model.Model
import org.scalatest.matchers.ShouldMatchers
import java.io.IOException
import org.scalatest.FunSpec
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.io.File
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.query.Query._


trait SparqlSuite extends FunSpec with ShouldMatchers {

   def passDir(model:Model, dirName: String) = {
	 val dir = new File(dirName)
     if (dir == null || dir.listFiles == null) 
        throw new IOException("Directory: " + dirName + " not accessible")
     else {
        for (file <- dir.listFiles ;
             if file.getName endsWith ".sparql") {
        val name = file.getName.dropRight(7) // remove ".sparql" = 7 chars 
        val contents = scala.io.Source.fromFile(file).mkString ;
        val query = QueryFactory.create(contents) 
        pass(name,query,model)
        }
      }
 }

  def pass(name : String, query: Query, model: Model) = {
    it("should pass integrity check: " + name) {
     val qe = QueryExecutionFactory.create(query,model)
     try {
       val reportModel = ModelFactory.createDefaultModel
       qe.execConstruct(reportModel)

       /* This is just to show the error that failed...could be removed or printed to logger */
       		if (reportModel.size != 0) { 
       			reportModel.write(System.out,"TURTLE")
       		} 
       reportModel.size should be(0);
     } finally {
       qe.close();
    }
   }
  }

  def notPass(name:String, query: Query, model: Model) = {
    it("should not pass integrity check: " + name) {
     val qe = QueryExecutionFactory.create(query,model)
     try {
       val reportModel = ModelFactory.createDefaultModel
       qe.execConstruct(reportModel)
       reportModel.size should be(0);
     } finally {
       qe.close();
     }
  }
 }

}
