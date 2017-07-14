package scheduler.workflows.mappers.functions

import io.vertx.core.json.JsonObject


/**
  * Created by Sascha on 03.07.2017.
  */
class JsonMerger(in:String, args:Map[String, String]) extends AbstractFunction[String, String](in, args) {
  override def t(): String = {
    val json = new JsonObject(args.getOrElse("json", "{}"))
    json.mergeIn(new JsonObject(in)).encode()
  }
}

object JsonMerger extends AbstractFunctionObj {
  val functionType = "JsonMerger"

  override def apply(in: Any, args: Map[String, Any]): AbstractFunction[_, _] = new JsonMerger(in.asInstanceOf[String], args.asInstanceOf[Map[String, String]])
}
