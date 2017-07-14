package scheduler.workflows.mappers.functions

import com.jayway.jsonpath.JsonPath

/**
  * Created by Sascha on 30.05.2017.
  */
class JsonExtractor(in:String, args:Map[String, String]) extends AbstractFunction[String, String](in, args) {
  def t():String = {
    JsonPath.read[String](in, args("jsonp"))
  }
}

object JsonExtractor extends AbstractFunctionObj {
  val functionType = "JsonExtractor"
  override def apply(in: Any, args: Map[String, Any]): AbstractFunction[_, _] = new JsonExtractor(in.asInstanceOf[String], args.asInstanceOf[Map[String, String]])
}

