package example

import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{JobDataMap, JobDetail, Scheduler, Trigger}
import scheduler.QuartzActionJob

/**
  * Created by Sascha on 01.07.2017.
  */
class Ex2QuartzScheduler {
  val sched:Scheduler =  StdSchedulerFactory.getDefaultScheduler
  sched.start()

  val jobData:JobDataMap = new JobDataMap()
  jobData.put("execute", "test")

  val job:JobDetail = newJob(classOf[QuartzActionJob]).setJobData(jobData).build()
  val trigger:Trigger = newTrigger().withSchedule(cronSchedule("/5 * * ? * *")).build()

  sched.scheduleJob(job, trigger)
}

object Ex2QuartzScheduler {
  def main(args: Array[String]): Unit = {
    new Ex2QuartzScheduler
  }
}
