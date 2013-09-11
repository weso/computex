import scala.annotation.implicitNotFound
import scala.concurrent.duration.DurationInt
import akka.actor.Props.apply
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.Props
import actors.AutoremoveActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    super.onStart(app)
    play.api.Play.mode(app) match {
      case play.api.Mode.Test => // do not schedule anything for Test
      case _ => autoremoveEARLDaemon(app)
    }

  }

  def autoremoveEARLDaemon(app: Application) {

    Logger.info("Schedulling \"Autoremove EARL-eports Demon\" every 48h")

    val autoremoveActor = Akka.system(app).actorOf(Props(new AutoremoveActor()))

    Akka.system(app).scheduler.schedule(
      24 hours,
      48 hours,
      autoremoveActor, "autoremoveEARLReports")
  }

}