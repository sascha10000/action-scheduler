package server

import java.util.Date

import helper.AuthHelper
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.{HttpHeaders, HttpMethod}
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.scala.ext.mongo.{FindOptions, MongoClient, MongoClientDeleteResult}
import io.vertx.scala.ext.web.RoutingContext
import org.quartz.{CronExpression, JobKey, Scheduler, Trigger}
import parser.ActionJsonParser
import scheduler.SchedulerJob
import helper.db.MongoHelper._
import helper.json.JsonObjOps
import io.netty.handler.codec.http.HttpHeaderValues
import org.quartz.impl.matchers.GroupMatcher

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * Created by Sascha on 01.07.2017.
  */
trait SchedulerRoutes {
  implicit val mongoDB:MongoClient
  implicit val execContext:ExecutionContext
  val scheduler:Scheduler
  val authHelper:AuthHelper

  val activateTestRoute:Boolean = true

  lazy val routes = Seq[(HttpMethod, String, (RoutingContext) => Unit)](
    (HttpMethod.POST, "/schedule", postSchedulerJob(false)),
    (HttpMethod.PUT, "/schedule", putSchedulerJob(false)),
    (HttpMethod.PUT, "/schedule/dbonly", putSchedulerJobDBOnly()),
    (HttpMethod.PUT, "/schedule/execute", putSchedulerJob(true)),
    (HttpMethod.POST, "/schedule/execute", postSchedulerJob(true)),
    (HttpMethod.POST, "/job/execute/:id", postJobForExecution),
    (HttpMethod.GET, "/schedule/:id/executed/:at", getJobWasExecuted),
    (HttpMethod.POST, "/schedules", postSchedules(false)),
    (HttpMethod.POST, "/schedules/execute", postSchedules(true)),
    (HttpMethod.POST, "/test/action", postAction),
    (HttpMethod.GET, "/schedule/:id", getScheduleById),
    (HttpMethod.GET, "/schedules/:authOn", getSchedulesData),
    (HttpMethod.GET, "/jobs", getScheduledJobs),
    (HttpMethod.DELETE, "/schedule/:id", deleteSchedule),
    (HttpMethod.POST, "/jobs/execute/byTimestamp", postDateForJobsToExecute),
    (HttpMethod.POST, "/jobs/execute/byCronExpression", postCronForJobsToExecute)
  )

  val getScheduleById:(RoutingContext) => Unit = (rc) => {
    val schedulerId = rc.request().getParam("id").get
    mongoDB.findOneFuture("jobs", new JsonObject().put("_id", schedulerId), null).onComplete {
      tryOne(rc, one)
    }
  }

  val getSchedulesData:(RoutingContext) => Unit = (rc) => {
    val findOptions = FindOptions().setFields(new JsonObject()
      .put("name",1)
      .put("meta", 1)
      .put("schedule", 1)
    )

    val authorizeOn = rc.request().getParam("authOn").get

    authHelper.findAuthorized("tasks", rc) onComplete {
      case Success(value) =>
        value match {
          case Left(body) =>
            val ids = new JsonArray(body)
            val allAllowed = (for (i <- 0 until ids.size() if ids.getString(i) == "*") yield true).nonEmpty
            if(allAllowed) getSchedulesByQuery(rc, new JsonObject(), findOptions)
            else getSchedulesByMeta(rc)(ids, findOptions, authorizeOn)

          case Right(status) => handleErr(rc, status, "{}")
        }
      case Failure(cause) => handleErr(rc, cause.getMessage)
    }
  }

  def getSchedulesByQuery(rc:RoutingContext, query:JsonObject, findOptions: FindOptions) = {
    mongoDB.findWithOptionsFuture("jobs", query, findOptions).onComplete {
      tryMany(rc, many)
    }
  }

  val getSchedulesByMeta = (rc:RoutingContext) => (ids:JsonArray, findOptions:FindOptions, metaField:String) => {
    val query = new JsonObject()
      .put("meta."+metaField, new JsonObject()
        .put(
          "$in", ids
        )
      )

    getSchedulesByQuery(rc, query, findOptions)
  }

  val getScheduledJobs:(RoutingContext) => Unit = (rc) => {
    def trigger2json(t:Trigger):JsonObject = {
      val jsObj = new JsonObject()

      if(t != null)
        jsObj
          .put("description", stringOrEmpty(t.getDescription))
          .put("calendarName",stringOrEmpty(t.getCalendarName))
          .put("previousFireTime", stringOrEmpty(t.getPreviousFireTime))
          .put("endTime", stringOrEmpty(t.getEndTime))
          .put("finalFireTime", stringOrEmpty(t.getFinalFireTime))
          .put("triggerKeyName", stringOrEmpty(t.getKey.getName))
          .put("nextFireTime", stringOrEmpty(t.getNextFireTime))
          .put("startTime", stringOrEmpty(t.getStartTime))
      else jsObj
    }

    def stringOrEmpty(obj:Any) = if(obj != null) obj.toString else ""

    val jsJobs = new JsonArray()

    scheduler.getJobGroupNames.forEach(groupName => {
      scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).forEach(jobKey => {
        val jobName = jobKey.getName
        val jobGroup = jobKey.getGroup

        val triggers = new JsonArray()
        val date = scheduler.getTriggersOfJob(jobKey).forEach(t => triggers.add(trigger2json(t)))


        jsJobs.add(new JsonObject().put("name", jobName).put("group", jobGroup).put("triggers", triggers))
      })
    })

    rc.response().putHeader(HttpHeaders.CONTENT_TYPE.toString, HttpHeaderValues.APPLICATION_JSON.toString())
    rc.response().end(jsJobs.encode())
  }

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

  val putSchedulerJob:(Boolean) => (RoutingContext) => Unit = (exec) => (rc) => {
    rc.request.bodyHandler((buff) => {
      val data = buff.toJsonObject
      try {
        if(!data.containsKey("_id")){
          rc.response().end("{\"error\" : \"The _id is empty.\"}")
        }
        else {
          val unscheduled = removeJob(data.getString("_id"))

          val sjob = scheduleJob(execute = exec, data)
          rc.response().end(new JsonObject().put("success", "Scheduled").put("msg", sjob.job.getJobDataMap).put("unscheduled", unscheduled).encode())
        }
      }
      catch{
        catchAny(rc)
      }
    })
  }

  val putSchedulerJobDBOnly:() => (RoutingContext) => Unit = () => (rc) => {
    rc.request.bodyHandler((buff) => {
      val data = buff.toJsonObject
      try {
        if(!data.containsKey("_id")){
          rc.response().end("{\"error\" : \"The _id is empty.\"}")
        }
        else {
          // makes the db changes
          mongoDB.removeFuture("jobs", new JsonObject().put("_id", data.getString("_id"))) onComplete (
            _ => mongoDB.saveFuture("jobs", data) onComplete( _ => {
                // reschedules the job
                val unscheduled = removeJob(data.getString("_id"), false)
                val sjob = scheduleJob(execute = false, data, false)

                rc.response().end(new JsonObject().put("success", "Scheduled").put("msg", sjob.job.getJobDataMap).put("unscheduled", unscheduled).encode())
              }
            )
          )
        }
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

  val deleteSchedule:(RoutingContext) => Unit = (rc) => {
    val schedulerId = rc.request().getParam("id").get
    removeJob(schedulerId)

    val query = new JsonObject()
      .put("_id", schedulerId)

    mongoDB.removeDocumentFuture("jobs", query) onComplete {
      case t:Try[MongoClientDeleteResult] =>
        rc.response().end(t.get.asJava.toJson.encodePrettily())
    }
  }

  // Gets a job by id (from the database) and executes it
  val postJobForExecution:(RoutingContext) => Unit = (rc) => {
    val id = rc.request().getParam("id").getOrElse("")

    if(id == "")
      rc.response().end("{\"error\" : \"ID not found or not sent.\"}")
    else {
      mongoDB.findOneFuture("jobs", new JsonObject().put("_id", id), new JsonObject()) onComplete {
        case Success(data) =>
          // maps the data
          val schedulerJob = SchedulerJob(data)

          // Schedules the new job
          val sjob = schedulerJob.scheduleJob(execute = true)(scheduler)

          rc.response().end(new JsonObject().put("data", schedulerJob.job.getJobDataMap).encode())
        case Failure(exception) =>
          rc.response().end("{\"error\" : \""+exception.getCause+"\"}")
      }
    }
  }

  // Params are ID of the Job and at which is the parameter for the time.
  // If you set at to 1 you'll get the information if the previous execution happened or not.
  // If you set at to 2 you'll get the information if the second previous execution happened or not and so on.
  val getJobWasExecuted:(RoutingContext) => Unit = (rc) => {
    val id = rc.request().getParam("id").getOrElse("")
    val at = rc.request().getParam("at").getOrElse("1").toInt

    if(id == "")
      rc.response().end("{\"error\" : \"ID not found or not sent.\"}")
    else {
      mongoDB.findOneFuture("jobs", new JsonObject().put("_id", id), new JsonObject()) onComplete {
          case Success(value) =>
            import org.quartz.CronExpression
            import java.text.SimpleDateFormat

            val cron = value.getString("schedule")
            val exp = new CronExpression(cron)
            val interval = getJobInterval(exp).getTime*at
            val preNext = exp.getNextValidTimeAfter(new Date(new Date().getTime - interval))

            val lower = preNext.getTime - (interval / 2)
            val upper = preNext.getTime + (interval / 2)

            // TODO: query not working

            val query = new JsonObject()
              .put("job",
                new JsonObject()
                  .put("id", id))
              .put("jobWasExecuted",
                new JsonObject()
                  .put("fireTime",
                    new JsonObject()
                      .put("$gt", JsonObjOps.convertToDate(new Date(lower)))
                      .put("$lt", JsonObjOps.convertToDate(new Date(upper)))))

            mongoDB.findOneFuture("executions", query,
              new JsonObject()) onComplete {
              case Success(_) => None
            }

          case Failure(exception) => rc.response().end("{\"error\" : \""+exception.getCause+"\"}")
      }
    }
  }

  // Executes all Job a given (via POST) matches
  // POST: { "match" : <some timestamp in milliseconds since .1970>
  val postDateForJobsToExecute:(RoutingContext) => Unit = (rc) => {
    rc.request().bodyHandler(body => {
      val date = body.toJsonObject.getLong("match")
      mongoDB.findFuture("jobs", new JsonObject()) onComplete {
        case Success(value) =>
          val jar = executeListOfJobsIf(value,
            (o) => {
              val cron = o.getString("schedule")
              val ex = new CronExpression(cron)

              ex.isSatisfiedBy(new Date(date))
            }
          )
          rc.response().end(jar.encodePrettily())
      }
    })
  }

  // Schedules and executes all jobs equal to a given cron-expression
  def postCronForJobsToExecute:(RoutingContext) => Unit = (rc) => {
    rc.request().bodyHandler(body => {
      val cron = body.toJsonObject.getString("match")

      mongoDB.findFuture("jobs", new JsonObject().put("schedule", cron)) onComplete {
        case Success(value) =>
          val jar = executeListOfJobsIf(value, _ => true)
          rc.response().end(jar.encodePrettily())
      }
    })
  }

  // Schedules and executes a given list of jobs if the given condition matches
  private def executeListOfJobsIf(buff:collection.mutable.Buffer[JsonObject], cond:(JsonObject) => Boolean) = {
    var jar = new JsonArray()
    buff.foreach(g => {
      if(cond(g)){
        scheduleJob(true, g)
        jar = jar.add(g)
      }
    })
    jar
  }

  private def getJobInterval(expression:CronExpression) = {
    val currentDate = new Date()
    val nextDate = expression.getNextValidTimeAfter(currentDate)
    val finterval = nextDate.getTime - currentDate.getTime
    val previousDate = expression.getNextValidTimeAfter(new Date(currentDate.getTime - finterval))
    new Date(nextDate.getTime - previousDate.getTime)
  }

  private def scheduleJob(execute:Boolean, data:JsonObject, saveToDB:Boolean = true) = {
    // maps the data
    val schedulerJob = SchedulerJob(data)

    //if(execute) schedulerJob.actions.foreach(a => a.execute(None))

    // Schedules the new job
    schedulerJob.scheduleJob(execute)(scheduler)

    // adds the job to the database
    if(saveToDB)
      mongoDB.saveFuture("jobs", data)

    schedulerJob
  }

  // removes the job by the given id
  private def removeJob(schedulerId:String, removeFromDB:Boolean = true):Boolean = {
    if(removeFromDB)
      mongoDB.removeFuture("jobs", new JsonObject().put("_id", schedulerId))

    scheduler.deleteJob(this.findJobKeyById(schedulerId))
  }

  // Executes the job once.
  // If it is not scheduled it won't be
  // If it is scheduled it will stay scheduled
  private def executeJobOnce(job:JsonObject) = {
    // maps the data
    val schedulerJob = SchedulerJob(job)

    //if(execute) schedulerJob.actions.foreach(a => a.execute(None))

    // Schedules the new job
    schedulerJob.scheduleJob(true, (_) => Unit, (job) => {removeJob(schedulerJob.id, false)})(scheduler)
    Unit
  }

  // finds the jobKey by the given id
  private def findJobKeyById(id:String): JobKey ={
    val jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup())

    jobKeys.forEach(g => {
      val jobDet = scheduler.getJobDetail(g)
      val jobId = jobDet.getJobDataMap.get("execute").asInstanceOf[SchedulerJob].id
      if(id == jobId)
        return g
    })

    null
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
