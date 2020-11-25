package server

import helper.AuthHelper
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.{LoggerFactory, SLF4JLogDelegateFactory}
import io.vertx.scala.ext.web.handler.CorsHandler
import io.vertx.lang.scala.{ScalaVerticle, VertxExecutionContext}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.mongo.MongoClient
import io.vertx.scala.ext.web.client.WebClient
import io.vertx.scala.ext.web.{Router, RoutingContext}
import org.quartz.impl.StdSchedulerFactory
import scheduler.SchedulerJob

import scala.util.{Failure, Success}

/**
  * Created by Sascha on 01.07.2017.
  */
class SchedulerVerticle extends ScalaVerticle {
  val log = LoggerFactory.getLogger(classOf[Nothing].getName) // Required for Logback to work in Vertx

  val customConfig = new JsonObject()
    .put("db_name", Config.schedulerservice.db.name)
    .put("host", Config.schedulerservice.db.host)
    .put("port", Integer.valueOf(Config.schedulerservice.db.port))

  //val execContext = VertxExecutionContext(vertx.getOrCreateContext())
  val globalScheduler = StdSchedulerFactory.getDefaultScheduler

  def routeLogger(rc:RoutingContext, f:RoutingContext => Unit): Unit = {
    println(rc.currentRoute())
    f(rc)
  }

  override def start(): Unit = {
    val router = Router.router(vertx)
    val mongoDb = MongoClient.createShared(vertx, customConfig)
    println("Scheduling existing Jobs...")
    scheduleExistingJobs(mongoDb)

    // handles all CORS requests
    router.route().handler(CorsHandler.create("*")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.PUT)
      .allowedMethod(HttpMethod.DELETE)
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedHeader("Authorization")
      .allowedHeader("content-type"))

    // adds all routes defined in the SchedulerRoutes
    val schedulerRoutes = new SchedulerRoutes {
      override val mongoDB: MongoClient = mongoDb
      override val scheduler = globalScheduler
      override val execContext = executionContext
      override val authHelper: AuthHelper = new AuthHelper(WebClient.create(vertx))
    }

    router.route().handler(g => {
      println("REQUEST")
      println(g.request().rawMethod()+"@" + g.request().absoluteURI())
      g.request().bodyHandler(h => println(h))
      g.next()
    })

    schedulerRoutes.routes.foreach(f => router.route(f._1, f._2).handler(rc => f._3(rc)))
    // on non matching routes
    router.route("/*").handler(g => {
      g.response().setStatusCode(404).end()
    })

    println(schedulerRoutes.routes)

    println("[STARTED] "+Config.schedulerservice.host+ ":" + Config.schedulerservice.port)
    vertx.createHttpServer().requestHandler(router.accept _).listen(Config.schedulerservice.port, Config.schedulerservice.host)
  }

  def scheduleExistingJobs(implicit db:MongoClient): Unit ={
    db.findFuture("jobs", new JsonObject()).onComplete {
      case Success(result) => {
        val jobs = result.map(f => {
          // converts json to SchedulerJob class
          val schedulerJob = SchedulerJob(f)
          schedulerJob.scheduleJob()(globalScheduler)
          schedulerJob
        })
      }
      case Failure(cause) => println(cause)
    }
    globalScheduler.start()
  }
}

object SchedulerVerticle {
  def main(args: Array[String]): Unit = {
    //System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, classOf[SLF4JLogDelegateFactory].getName)
    Vertx.vertx.deployVerticle(ScalaVerticle.nameForVerticle[SchedulerVerticle])
  }
}
