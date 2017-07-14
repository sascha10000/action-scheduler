package helper.json

import helper.Ops
import io.vertx.lang.scala.json.{JsonArray, JsonObject}

import scala.collection.JavaConverters._

/**
  * Created by Sascha on 01.06.2017.
  */
class JSON(data:JsonObject){
  def asOption[T1](get:String):Option[T1] = {
    Ops.asOption[AnyRef, T1](data.getValue(get), (a) => a.asInstanceOf[T1])
  }

  def attributesAsMap():Map[String, Any] = {

    /*
    {
    data.getMap.asScala.foldLeft(Map[String, Any]())((prev, el) => if(el._2.isInstanceOf[JsonObject] || el._2.isInstanceOf[JsonArray]) {
      prev + (el._1 -> el._2)
    }
    else {
      prev + (el._1 -> el._2.toString)
    })
  }*/
    val fieldNames = data.fieldNames()
    var map: Map[String, Any] = Map()
    fieldNames.forEach((a) => {
      map = map + (a -> data.getValue(a))
    })

    map
  }
}

trait JSONDecorator {
  implicit def toDecorator(json:JsonObject):JSON = new JSON(json)
}

object JSON extends JSONDecorator
