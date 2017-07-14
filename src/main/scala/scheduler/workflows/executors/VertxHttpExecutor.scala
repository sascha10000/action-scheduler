package scheduler.workflows.executors

import helper.Ops
import helper.http.Methods
import io.vertx.core.buffer.Buffer
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient}
import scheduler.workflows.actions.HttpRequestAction
import scheduler.workflows.executors.VertxHttpExecutor.VertxRespType
import scheduler.workflows.mappers.HttpResponseMapper

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by Sascha on 26.05.2017.
  */
class VertxHttpExecutor(val client:WebClient, action:Option[HttpRequestAction]) extends Executor[Option[VertxRespType], VertxRespType] {
  override def execute(data:Option[VertxRespType]):VertxRespType = {
    val response = Ops.ifNotNone(data, () => (Await.result(data.get, 10 seconds)))
    val mapper = new HttpResponseMapper(this.action.getOrElse(new HttpRequestAction("", Methods.GET, "", client)), response)
    val action = mapper.exec()

    val port = if(action.url.getPort <= 0) 80 else action.url.getPort

    // creates the request object
    val request = action.method match {
      case Methods.GET => client.get(port, action.url.getHost, action.url.getPath)
      case Methods.POST => client.post(port, action.url.getHost, action.url.getPath)
      case Methods.PUT => client.put(port, action.url.getHost, action.url.getPath)
      case Methods.DELETE => client.delete(port, action.url.getHost, action.url.getPath)
      case Methods.HEAD => client.head(port, action.url.getHost, action.url.getPath)
      case Methods.OPTIONS => ???
    }

    // sets the headers
    action.headers.getOrElse(Map()).foreach(f => request.putHeader(f._1, f._2))
    // sets the params
    action.params.getOrElse(Map()).foreach(f => request.addQueryParam(f._1, f._2))
    // sends the request and sets the body
    val resp = request.sendBufferFuture(Buffer.buffer(action.body))

    resp
  }
}

object VertxHttpExecutor {
  type VertxRespType = Future[HttpResponse[Buffer]]
}
