package helper.db

import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.json.JsonArray
import io.vertx.scala.ext.web.RoutingContext

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * Created by Sascha on 21.04.2017.
  */
object MongoHelper {
  // some types
  type JsonPartialFunction[T] =  PartialFunction[Try[T], Unit]
  type OneJ = JsonObject
  type ManyJ = mutable.Buffer[OneJ]
  type One = JsonPartialFunction[OneJ]
  type Many = JsonPartialFunction[ManyJ]
  type RCHandler = (RoutingContext) => Unit

  //  creates an JsonObject with the field _id as mongodb id of the document ({"id": <id>})
  def jsonId(id:Option[String]):JsonObject = new JsonObject().put("_id", id.getOrElse(""))

  // puts the requested route in the response
  def testResponse(implicit rc:RoutingContext) = rc.response.end(rc.request().absoluteURI() + " WORKS")

  // sends an error message if occurring
  def handleErr(rc:RoutingContext, msg:String) = {
    rc.response().setStatusCode(404)
    rc.response().end(new JsonObject().put("errMsg", msg).encode())
  }
  def handleErr(rc:RoutingContext, statusCode:Int,msg:String) = {
    rc.response().setStatusCode(statusCode)
    rc.response().end(new JsonObject().put("errMsg", msg).encode())
  }

  def tryOne(rc:RoutingContext, op:(OneJ) => String): One = {
    case Success(result) => rc.response().end(op(result))
    case Failure(cause) => handleErr(rc, cause.getMessage)
  }

  def tryMany(rc:RoutingContext, op:(ManyJ) => String):Many = {
    case Success(result) => rc.response().end(op(result))
    case Failure(cause) => handleErr(rc, cause.getMessage)
  }

  def tryString(rc:RoutingContext):JsonPartialFunction[String] = {
    case Success(result) => rc.response().end(new JsonObject().put("_id", result).encode())
    case Failure(cause) => handleErr(rc, cause.getMessage)
  }

  def tryJsonArray(rc:RoutingContext):JsonPartialFunction[JsonArray] = {
    case Success(result) => rc.response().end(result.encode())
    case Failure(cause) => handleErr(rc, cause.getMessage)
  }

  // functions that convert JsonObject to String (as one Object or Array) used for functional composition
  val one = (data:OneJ) => if(data != null) data.encode() else new JsonObject().encode()
  val many = (data:ManyJ) => data.foldLeft("[")((prev, elem) => (if (prev != "[") prev + "," else prev) + elem.encode()) + "]"
}
