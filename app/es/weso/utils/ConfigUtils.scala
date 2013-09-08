package es.weso.utils

import com.typesafe.config._
import java.net.URI

object ConfigUtils {

  def getName(conf: Config, name: String) : String = {
    if (conf.hasPath(name)) {
      conf.getString(name)
    } else {
       throw new Exception("config.getFileName: config: " + conf + " does not contain entry for " + name)
    }
  }
  
  def getURI(conf: Config, name: String) : URI = {
    
    if (conf.hasPath(name)) {
      val str = conf.getString(name)
      try {
        new URI(str)
      } catch {
        case e: Exception => throw new Exception("config.getURI: Cannot get URI from " + str + " obtained from name: " + name + " at config: " + conf)
      }
    }
    else throw new Exception("config.getURI: config: " + conf + " does not contain entry for " + name)

  }

}