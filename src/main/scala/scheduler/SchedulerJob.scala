package scheduler

import java.util.Date

import io.vertx.core.json.JsonArray
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.mongo.MongoClient
import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.impl.matchers.EverythingMatcher._

import org.quartz.{JobDataMap, JobDetail, Scheduler}
import parser.ActionJsonParser

import scala.collection.JavaConverters._

/**
  * Created by Sascha on 01.07.2017.
  */
class SchedulerJob(val name:String, val schedule:String, val meta:String, val execute:String)(implicit val mongodb:MongoClient) {
  private val jsonActions = new JsonArray(execute)
  val listener = new ExecutionListener(mongodb)

  val actions = for { i <- 0 until jsonActions.size() }
    yield new ActionJsonParser(jsonActions.getJsonObject(i)).action

  actions.foreach(println)

  def scheduleJob:(Scheduler) => Date = {
    val jobData = new JobDataMap(Map(("execute", this)).asJava)
    val job:JobDetail = newJob(classOf[QuartzActionJob])
      .setJobData(jobData)
      .build()

    val trigger = newTrigger().withSchedule(cronSchedule(schedule)).build()

    (scheduler:Scheduler) => {
      scheduler.getListenerManager.addJobListener(listener, allJobs())
      scheduler.getListenerManager.addTriggerListener(listener, allTriggers())
      scheduler.scheduleJob(job, trigger)
    }
  }
}

object SchedulerJob {
  def apply(obj:JsonObject)(implicit mongodb:MongoClient): SchedulerJob = {
    val name = obj.getString("name")
    val schedule = obj.getString("schedule")
    val actions = obj.getJsonArray("actions").encode()
    val meta = if(obj.containsKey("meta")) obj.getJsonObject("meta").encode() else "{}"

    new SchedulerJob(name, schedule, meta, actions)
  }
}
