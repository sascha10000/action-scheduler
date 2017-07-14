package scheduler.workflows.mappers.functions

import io.vertx.core.json.JsonObject

/**
  * Created by Sascha on 04.07.2017.
  */
class JsonFieldReplacer(in:String, args:Map[String, Any]) extends AbstractFunction[String, Any](in, args) {
  override def t(): String = {
    val replace = args.getOrElse("replace","").toString
    // not type agnostic. Its only working for String at the moment
    val replaceWith = args.getOrElse("with", "")

    val json = new JsonObject(in)
    json.remove(replace)

    json.put(replace, replaceWith).encode()
  }
}

object JsonFieldReplacer extends AbstractFunctionObj {
  val functionType = "JsonFieldReplacer"

  override def apply(in: Any, args: Map[String, Any]): AbstractFunction[_, _] = {
    new JsonFieldReplacer(in.asInstanceOf[String], args.asInstanceOf[Map[String, String]])
  }
}
