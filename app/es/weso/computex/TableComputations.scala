package es.weso.computex

import org.rogach.scallop.Scallop
import com.typesafe.config.ConfigFactory
import org.rogach.scallop.ScallopConf
import org.rogach.scallop.exceptions.Help
import org.slf4j.LoggerFactory
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.util.FileManager
import com.hp.hpl.jena.rdf.model.Model
import scala.io.Source
import es.weso.utils.JenaUtils._
import com.hp.hpl.jena.query.ResultSet
import play.api.libs.json._
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.FileOutputStream
import com.hp.hpl.jena.rdf.model.SimpleSelector
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.rdf.model.Property
import PREFIXES._
import scala.collection.JavaConverters
import es.weso.computex.profile.Profile
import es.weso.utils.StatsUtils._

case class TableComputations(table: scala.collection.mutable.HashMap[(Resource,Resource),Option[Double]]) {

  val areas = scala.collection.mutable.Set[Resource]() 

  def insert(i: Resource, a: Resource, v: Double) : Unit = {
   insert(i,a,Some(v))
  }

def insert(i: Resource, a: Resource, v: Option[Double]) : Unit = {
  areas += a
  table.put((i,a),v)
}

def contains(i: Resource, a: Resource) : Boolean = {
  if (table.contains((i,a))) lookup(i,a) != None
  else false
}

def lookup(i : Resource, a: Resource) : Option[Double] = {
  if (table.contains((i,a))) table((i,a))
  else None
}

def getAreas: Set[Resource] = {
  areas.toSet
}

def group(groupings: Map[Resource,Set[Resource]],
		  weights:Map[Resource,Double]
         ): TableComputations = {
   val areas = getAreas
   val result = TableComputations.empty
   for (c <- groupings.keys ; a <- areas) {
     val vs = (for (i <- groupings(c); if lookup(i,a) != None) 
               yield (lookup(i,a).get,weights(i))).toSeq
     result.insert(c,a,weightedAvg(vs))
   }
   result
}

def weightedAvg(vs : Seq[(Double,Double)]) : Option[Double] = {
   if (vs.isEmpty) None
   else {
     val sum = vs.foldLeft(0.0)((r,p)=> p._1 * p._2 + r)
     Some(sum / vs.length)
   }
 }

 def getIndicators : Iterable[Resource] = 
   table.keys.map(p => p._1) 

 def show: Unit = {
   print("         ")
   getIndicators.foreach(i => print(i + " "))
   println
   getAreas.foreach(a => {
     print(a + " " )
     getIndicators.foreach(i => print(lookup(i,a) + "     "))
     println
   })
 }

 def showInfo: Unit = {
   println("Table. Areas: " + getAreas.size + ", Indicators: " + getIndicators.size)
   println("Areas: " + getAreas)
   println("Indicators: " + getIndicators)
 }
}

object TableComputations {
  val empty = TableComputations(scala.collection.mutable.HashMap[(Resource,Resource),Option[Double]]())

}