package scheduler

import helper.json.JsonObjOps
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.mongo.MongoClient
import io.vertx.scala.ext.web.client.HttpResponse
import org.quartz._
import org.quartz.listeners.TriggerListenerSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Sascha on 26.05.2017.
  */
class QuartzActionJob() extends Job {
  override def execute(context: JobExecutionContext): Unit = {
    val ret = context.getMergedJobDataMap.get("execute").asInstanceOf[SchedulerJob]
    ret.listener.saveAndAdd(new JsonObject().put("name", ret.name).put("cronSchedule", ret.schedule).put("id", ret.id))
    ret.actions.foreach(f => {
      val execRes = f.execute(None)

      /* TODO: DEBUG CODE
      execRes.asInstanceOf[Future[Any]].onComplete{
        case Success(s) if s.isInstanceOf[HttpResponse[Any]] => println(s.asInstanceOf[HttpResponse[Any]].body())
        case Success(s) => println(s)
        case Failure(f) => println(f)
      }*/
    })
  }
}

class ExecutionListener(mongodb:MongoClient) extends TriggerListenerSupport with JobListener {
  var execData:JsonObject = new JsonObject

  // Trigger Listener
  override def triggerComplete(trigger: Trigger, context: JobExecutionContext, triggerInstructionCode: Trigger.CompletedExecutionInstruction): Unit = {
    tryCatch { () =>  execData = execData.put("triggerComplete", triggerJsonData(trigger)) }
  }

  override def triggerFired(trigger: Trigger, context: JobExecutionContext): Unit = {
    tryCatch { () => execData = execData.put("triggerFired", triggerJsonData(trigger)) }
  }

  override def triggerMisfired(trigger: Trigger): Unit = {
    tryCatch { () => execData = execData.put("triggerMisfired", triggerJsonData(trigger)) }
  }

  private def triggerJsonData(trigger:Trigger):JsonObject = {
    val data = new JsonObject
    data.put("finalFireTime", JsonObjOps.convertToDate(trigger.getFinalFireTime))

    update
    data
  }

  // JobListener
  override def getName: String = getClass.getName + "@" + System.identityHashCode(this)

  override def jobToBeExecuted(context: JobExecutionContext): Unit = {
    tryCatch { () => execData = execData.put("jobToBeExecuted", jobJsonData(context)) }
  }

  override def jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException): Unit = {
    tryCatch { () => execData = execData.put("jobWasExecuted", jobJsonData(context)) }
  }

  override def jobExecutionVetoed(context: JobExecutionContext): Unit = {
    tryCatch { () => execData = execData.put("jobExecutionVetoed", jobJsonData(context)) }
  }

  private def jobJsonData(context:JobExecutionContext):JsonObject = {
    val data = new JsonObject
    data.put("fireTime", JsonObjOps.convertToDate(context.getFireTime))
    data.put("instance", if(context.getJobInstance != null) context.getJobInstance.toString else "")
    data.put("runtime", if(context.getJobRunTime != null) context.getJobRunTime else "")

    update

    data
  }

  private def tryCatch(f:() => Unit): Unit = {
    try {
      f()
    }
    catch {
      case e:Exception => e.printStackTrace()
    }
  }

  def saveAndAdd(jso:JsonObject): Unit = {
    mongodb.saveFuture("executions", execData.put("job", jso))onComplete {
      case Success(value) =>
        addId(value)
    }
  }

  def update():Unit = {
    if(execData.containsKey("job"))
      mongodb.saveFuture("executions", execData) onComplete {
        case Success(value) => addId(value)
        case Failure(exception) =>
          println(exception)
      }
  }

  private def addId(value:String): Unit = {
    if((value != "" || value != null) && !execData.containsKey("_id"))
      execData = execData.put("_id", value)
  }


}
