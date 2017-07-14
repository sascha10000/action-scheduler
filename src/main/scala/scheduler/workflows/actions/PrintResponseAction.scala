package scheduler.workflows.actions

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.web.client.HttpResponse
import scheduler.workflows.executors.Executor
import scheduler.workflows.executors.VertxHttpExecutor.VertxRespType

import scala.concurrent.{Await, Future}
import scala.collection.JavaConverters._

/**
  * Created by Sascha on 28.05.2017.
  *
  * Blocks this part of the chain, prints the response of the previous action's request, and executes the next actions by forwarding the previous action's response (not it's own as HttpAction).
  * Crashes if previous Action has no data!
  */
case class PrintResponseAction(override val name:String,
                               override val next: Option[List[AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]]]] = None,
                               override val predecessor: Option[AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]]] = None,
                               override val mappings: Option[String] = None) extends AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]] {

  override val executor: Option[Executor[Option[VertxRespType], HttpResponse[Buffer]]] = None

  override val actionType: String = PrintResponseAction.actionType

  override def execute(data: Any): VertxRespType = {
    val prevData = data.asInstanceOf[Option[VertxRespType]]
    prevData match {
      case Some(data) =>
        import scala.concurrent.duration._
        val respdata = Await.result(data, 10 seconds)
        println("[Action "+this.predecessor.get.name+"]")
        println("Response Data: " + respdata.bodyAsString())
        println("Headers:")
        respdata.headers().names().foreach(name => {
          print(name + " -> ")
          respdata.headers().getAll(name).foreach(println)
        })
      case None =>
        println("NO PREDECESSOR")
        None
    }

    for (elem <- next.getOrElse(List())) {elem.predecessor(this).execute(data)}

    prevData.get
  }

  override def predecessor(newPredecessor: AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]]): AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]] = PrintResponseAction(name, next, Some(newPredecessor))

  def next(abstractAction: AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]]):AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]] = {
    val newNext = this.next match {
      case None => List(abstractAction)
      case Some(actions) => actions :+ abstractAction
    }

    PrintResponseAction(name, Some(newNext), predecessor)
  }

  override def asJson: JsonObject = new JsonObject().put("type", PrintResponseAction.actionType).put("name", this.name).put("next", this.next.getOrElse(List()).foldLeft(new JsonArray())((prev, elem) => prev.add(elem.asJson)))

  override def mappings(newMappings: String): AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]] = new PrintResponseAction(name, next, predecessor, Some(newMappings))
}

object PrintResponseAction {
  val actionType = "PrintResponseAction"
}
