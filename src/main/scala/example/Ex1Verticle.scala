package example

import helper.http.Methods
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.Router
import io.vertx.scala.ext.web.client.WebClient
import parser.ActionJsonParser
import scheduler.workflows.actions.HttpRequestAction

import scala.util.{Failure, Success}

/**
  * Created by Sascha on 27.05.2017.
  */
class Ex1Verticle extends ScalaVerticle {
  override def start(): Unit = {
    val router = Router.router(vertx)
    val client = WebClient.create(vertx)

    // 1. Create Action
    // 2. Create Executor (needs the action data)
    // 3. Add the Exectuor to the action and execute
    val action = new HttpRequestAction("Test", Methods.GET, "http://lcoalhost:3000/persons", client)
    action.execute(None)

    router.route(HttpMethod.POST, "/action").handler(f => {
      f.request().bodyHandler(b => {
        val act = new ActionJsonParser(b.toJsonObject).action
        val resp = act.execute(None)

        resp.onComplete {
          case Success(s) => f.response().end(Buffer.buffer(s.bodyAsString().get))
          case Failure(s) => f.response().end(Buffer.buffer(s.getMessage))
        }
      })
    })

    vertx.createHttpServer().requestHandler(router.accept _).listen(3001)
  }
}

object Ex1Verticle {
  def main(args: Array[String]): Unit = {
    Vertx.vertx.deployVerticle(ScalaVerticle.nameForVerticle[Ex1Verticle])
  }
}
