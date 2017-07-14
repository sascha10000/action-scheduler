package server

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.scala.ext.mongo.MongoClient
import io.vertx.scala.ext.web.RoutingContext
import org.quartz.Scheduler
import parser.ActionJsonParser
import scheduler.SchedulerJob

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by Sascha on 01.07.2017.
  */
trait SchedulerRoutes {
  implicit val mongoDB:MongoClient
  implicit val execContext:ExecutionContext
  val scheduler:Scheduler

  val activateTestRoute:Boolean = true

  lazy val routes = Seq[(HttpMethod, String, (RoutingContext) => Unit)](
    (HttpMethod.POST, "/schedule", postSchedulerJob(false)),
    (HttpMethod.POST, "/schedule/execute", postSchedulerJob(true)),
    (HttpMethod.POST, "/schedules", postSchedules(false)),
    (HttpMethod.POST, "/schedules/execute", postSchedules(true)),
    (HttpMethod.POST, "/test/action", postAction)
  )

  val postSchedulerJob:(Boolean) => (RoutingContext) => Unit = (exec) => (rc) => {
    rc.request.bodyHandler((buff) => {
      val data = buff.toJsonObject

      try {
        scheduleJob(execute = exec, data)
        rc.response().end(new JsonObject().put("success", "Scheduled").put("msg", data).encode())
      }
      catch{
        catchAny(rc)
      }
    })
  }

  val postSchedules:(Boolean) => (RoutingContext) => Unit = (exec) => (rc) => {
    rc.request.bodyHandler((buff) => {
      val data = buff.toJsonArray

      try {
        for (i <- 0 until data.size()) {
          scheduleJob(execute = exec, data.getJsonObject(i))
        }

        rc.response().end(new JsonObject().put("success", "Scheduled" + (if(exec) " and executed" else "")).put("msg", data).encode())
      }
      catch{
        catchAny(rc)
      }
    })
  }

  val postAction:(RoutingContext) => Unit = (rc) => if(activateTestRoute){
    rc.request().bodyHandler(b => {
      try {
        val act = new ActionJsonParser(b.toJsonObject).action
        val resp = act.execute(None)

        resp.onComplete {
          case Success(s) => rc.response().end(Buffer.buffer(s.bodyAsString().getOrElse("{}")))
          case Failure(s) => rc.response().end(Buffer.buffer(s.getMessage))
        }
      }
      catch{
        case e:Exception => {
          val fullMsg = e.getStackTrace.foldLeft(new JsonArray())((prev, el) => {
            prev.add(new JsonObject().put("ClassName", el.getClassName).put("Class", el.getClass.toString).put("MethodName", el.getMethodName).put("FileName", el.getFileName).put("Line", el.getLineNumber))
            prev
          })
          rc.response().end(new JsonObject().put("error", "An exception occured").put("msg", e.getMessage).put("stacktrace", fullMsg).encode())
        }
      }
    })
  }

  private def scheduleJob(execute:Boolean, data:JsonObject) = {
    // maps the data
    val schedulerJob = SchedulerJob(data)

    if(execute) schedulerJob.actions.foreach(a => a.execute(None))

    // Schedules the new job
    schedulerJob.scheduleJob(scheduler)

    // adds the job to the database
    mongoDB.saveFuture("jobs", data)
  }

  private def catchAny(rc:RoutingContext):PartialFunction[Throwable, Any] = {
    case e:Exception => {
      val fullMsg = e.getStackTrace.foldLeft(new JsonArray())((prev, el) => {
        prev.add(new JsonObject().put("ClassName", el.getClassName).put("Class", el.getClass.toString).put("MethodName", el.getMethodName).put("FileName", el.getFileName).put("Line", el.getLineNumber))
        prev
      })
      rc.response().end(new JsonObject().put("error", "An exception occured").put("msg", e.getMessage).put("stacktrace", fullMsg).encode())
    }
  }
}
