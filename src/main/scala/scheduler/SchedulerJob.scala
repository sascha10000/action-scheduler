package scheduler

import java.util.Date

import io.vertx.core.json.JsonArray
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.mongo.MongoClient
import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.impl.matchers.EverythingMatcher._
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.{JobDataMap, JobDetail, JobKey, Scheduler}
import parser.ActionJsonParser

import scala.collection.JavaConverters._

/**
  *
  * Created by Sascha on 01.07.2017.
  *
  */
class SchedulerJob(val name:String, val schedule:String, val meta:String, val execute:String, val id:String)(implicit val mongodb:MongoClient) {
  private val jsonActions = new JsonArray(execute)
  val listener = new ExecutionListener(mongodb)

  val actions = for { i <- 0 until jsonActions.size() }
    yield new ActionJsonParser(jsonActions.getJsonObject(i)).action

  actions.foreach(println)

  val jobData = new JobDataMap(Map(("execute", this)).asJava)
  val job:JobDetail = newJob(classOf[QuartzActionJob])
    .setJobData(jobData)
    .build()

  def scheduleJob(execute:Boolean = false, ifScheduled:(JobDetail) => Unit = (_) => Unit, ifNotScheduled:(JobDetail) => Unit = (_) => Unit):(Scheduler) => Date = {
    val trigger = newTrigger().withSchedule(cronSchedule(schedule)).build()

    (scheduler:Scheduler) => {
      val key:JobKey = findKeyJob(this.id, scheduler)

      scheduler.checkExists(key) match {
        case true => println("Job "+ job.getKey + " already scheduled")

          ifScheduled(job)

          if(execute) scheduler.triggerJob(key)
          new Date()
        case false =>
          scheduler.getListenerManager.addJobListener(listener, allJobs())
          scheduler.getListenerManager.addTriggerListener(listener, allTriggers())
          val r = scheduler.scheduleJob(job, trigger)

          ifNotScheduled(job)

          if(execute) scheduler.triggerJob(job.getKey)

          r
      }
    }
  }

  def findKeyJob(id:String, sched:Scheduler):JobKey = {
    val jobKeys = sched.getJobKeys(GroupMatcher.anyJobGroup())
    var found: JobKey = null
    jobKeys.forEach(g => {
      val jobDet = sched.getJobDetail(g)
      val jobId = jobDet.getJobDataMap.get("execute").asInstanceOf[SchedulerJob].id
      if(this.id == jobId)
        found = g
    })

    found
  }
}

object SchedulerJob {
  def apply(obj:JsonObject)(implicit mongodb:MongoClient): SchedulerJob = {
    val id = if(obj.containsKey("_id")) obj.getString("_id") else ""
    val name = obj.getString("name")
    val schedule = obj.getString("schedule")
    val actions = obj.getJsonArray("actions").encode()
    val meta = if(obj.containsKey("meta")) obj.getJsonObject("meta").encode() else "{}"

    new SchedulerJob(name, schedule, meta, actions, id)
  }
}
