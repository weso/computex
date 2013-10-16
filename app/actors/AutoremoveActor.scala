package actors

import com.github.nscala_time.time.Imports._
import akka.actor.Actor
import java.io.File
import play.api.Logger

class AutoremoveActor extends Actor {
  def receive = {
    case _ =>
      
      Logger.info("\"Autoremove EARL-eports Demon\" is waking up")
      
      val limit = DateTime.now - 48.hours
      val dir = new File("public/earls/")
      for {
        file <- dir.listFiles.filterNot(_.getName == ".gitkeep")
        fileM = new DateTime(file.lastModified)
        if limit > fileM
      } {
        if (file.delete) {
          Logger.info("EARL Report removed: " + file.getAbsoluteFile)
        } else {
          Logger.warn("EARL Report can not be removed: " + file.getAbsoluteFile)
        }
      }
      
      Logger.info("\"Autoremove EARL-eports Demon\" is going to sleep")
  }
}