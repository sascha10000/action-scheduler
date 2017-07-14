package parser

import helper.Ops.asOption
import io.vertx.lang.scala.json.{JsonArray, JsonObject}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.WebClient
import scheduler.workflows.actions.{AbstractAction, HttpRequestAction, PrintResponseAction}
import scheduler.workflows.executors.VertxHttpExecutor.VertxRespType

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by Sascha on 28.05.2017.
  *
  * Maps an AbstractAction from JSON to a concrete Object. It is a Factory.
  */
class ActionJsonParser(jsonObject: JsonObject) {
  val typee = jsonObject.getString("type")
  val name = jsonObject.getString("name")

  val next: List[AbstractAction[Option[VertxRespType], VertxRespType]] = if(jsonObject.containsKey("next")) {
    jsonObject.getJsonArray("next").asScala.map(f => {
      val act = f.asInstanceOf[JsonObject]
      new ActionJsonParser(act)
    }).foldLeft(List[AbstractAction[Option[VertxRespType], VertxRespType]]())((prev, elem) => {
      prev :+ elem.action
    })
  }
  else List[AbstractAction[Option[VertxRespType], VertxRespType]]()

  val mappings: Option[String] = asOption[JsonArray, String](jsonObject.getJsonArray("mappings"), (f:JsonArray) => { f.encode() })

  // Factory-method, according to the type it will parse individual data by additional parsers and create the action.
  val action:AbstractAction[Option[VertxRespType], VertxRespType] = typee match {
    case HttpRequestAction.actionType =>
      val json = new JsonHttpActionParser(jsonObject)
      val client = WebClient.create(Vertx.vertx())
      val act = new HttpRequestAction(name, json.method, json.url, json.body, client, Some(json.params), Some(json.headers), mappings, None, None)

      detNext(act)
    case PrintResponseAction.actionType =>
      val json = new JsonPrintResponseActionParser(jsonObject)
      val act = new PrintResponseAction(name)
      detNext(act)
  }

  def detNext(act:AbstractAction[Option[VertxRespType], VertxRespType]) = {
    next.foldLeft(act)((prev,elem) => {
      prev.next(elem)
    })
  }
}
