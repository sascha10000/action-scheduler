package helper.json

import java.util.Date

import io.vertx.lang.scala.json.JsonObject

object JsonObjOps {
  def convertToDate(d:Date): JsonObject ={
    if(d != null)
      new JsonObject().put("$date", d.toInstant)
    else
      new JsonObject()
  }

}
