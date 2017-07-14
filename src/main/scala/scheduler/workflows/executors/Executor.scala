package scheduler.workflows.executors

import io.vertx.core.buffer.Buffer
import io.vertx.scala.ext.web.client.HttpResponse
import scheduler.workflows.actions.{AbstractAction, HttpRequestAction}
import scheduler.workflows.mappers.{AbstractMapper, HttpResponseMapper}

import scala.concurrent.Future

/**
  * Created by Sascha on 26.05.2017.
  */
abstract class Executor[T, R] {
  def execute(data:T):Future[R]

  def getMapperInstance(mapper:String, response:Option[R], action:AbstractAction[_,_, _], default:AbstractMapper[_,_,_]):AbstractMapper[_,_,_] = {
    if(mapper == "HttpResponseMapper")
       new HttpResponseMapper(action.asInstanceOf[HttpRequestAction], response.asInstanceOf[Option[HttpResponse[Buffer]]])
    else
      default
  }
}
