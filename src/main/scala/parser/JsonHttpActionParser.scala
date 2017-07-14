package parser

import helper.http.Methods
import io.vertx.lang.scala.json.{JsonArray, JsonObject}

import scala.collection.JavaConverters._
import helper.Ops._

/**
  * Created by Sascha on 29.05.2017.
  */
class JsonHttpActionParser(jsonObject: JsonObject) {
  val method: Methods.Value = Methods.withName(jsonObject.getString("method"))
  val url: String = jsonObject.getString("url")
  val body: String = jsonObject.getString("body")

  val params: Map[String, String] = if(jsonObject.containsKey("params")) {
    jsonObject.getJsonArray("params").asScala.map(f => {
      val attr = f.asInstanceOf[JsonObject].getString("attribute")
      val valu = f.asInstanceOf[JsonObject].getString("value")

      (attr, valu)
    }).foldLeft(Map[String, String]())((prev, elem) => {
      prev + elem
    })
  }
  else Map[String, String]()

  val headers: Map[String, String] = if(jsonObject.containsKey("headers")) {
    jsonObject.getJsonArray("headers").asScala.map(f => {
      val attr = f.asInstanceOf[JsonObject].getString("attribute")
      val valu = f.asInstanceOf[JsonObject].getString("value")

      (attr, valu)
    }).foldLeft(Map[String, String]())((prev, elem) => {
      prev + elem
    })
  }
  else Map[String, String]()
}
