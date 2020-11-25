package scheduler.workflows.mappers.functions

import io.vertx.core.json.{JsonArray, JsonObject}


/**
  * Created by Sascha on 04.07.2017.
  */
class JsonArrayResetter(in:String, args:Map[String, Any]) extends AbstractFunction[String, Any](in, args) {
  override def t(): String = {
    val json = new JsonObject(in)
    val arrayName = args.getOrElse("arrayName", "").asInstanceOf[String]

    val resetJson = new JsonObject(args.getOrElse("reset", "{}").toString)


    val array = json.getJsonArray(arrayName)
    json.remove(arrayName)

    val newObjects = for (i <- 0 until array.size()) yield array.getJsonObject(i).mergeIn(resetJson, true)
    val newArray = newObjects.foldLeft(new JsonArray())((prev, el) => prev.add(el))

    json.put(arrayName, newArray).encode()
  }
}

object JsonArrayResetter extends AbstractFunctionObj {
  override val functionType: String = "JsonArrayResetter"

  override def apply(in: Any, args: Map[String, Any]): AbstractFunction[_, _] = new JsonArrayResetter(in.asInstanceOf[String], args.asInstanceOf[Map[String, Any]])
}
