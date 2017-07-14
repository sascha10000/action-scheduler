package scheduler.workflows.mappers

import java.text.SimpleDateFormat
import java.util.Date

import helper.json.JSON._
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.web.client.HttpResponse
import scheduler.workflows.actions.{AbstractAction, HttpRequestAction}
import scheduler.workflows.executors.VertxHttpExecutor.VertxRespType

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * Created by Sascha on 29.05.2017.
  */
class HttpResponseMapper(val action:HttpRequestAction, override val input:Option[HttpResponse[Buffer]]) extends AbstractMapper[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]](action){
  import HttpResponseMapper._
  override val inAction = resolvePredecessor(action)

  /** The Response mapper needs an predecessor of type HttpAction and is therefore extracting the last predecessor matching that type.
    *
    * @param action
    * @return
    */
  @Override
  override def resolvePredecessor(action:AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]]):Option[AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]]] = {
    if(action.predecessor.isEmpty) None
    else if(action.predecessor.get.isInstanceOf[HttpRequestAction])
      action.predecessor
    else
      resolvePredecessor(action.predecessor.get)
  }

  /** Executes the functions defined in action.mappings but if and only if a predecessor (inAction) and mappings are defined
    *
    * @return the modified action with the transformed to fields
    */
  @Override
  override def exec(): HttpRequestAction = if(action.mappings.isDefined && inAction.isDefined && input.isDefined){
    val inAction = this.inAction.get.asInstanceOf[HttpRequestAction]
    val jsonar = new JsonArray(action.mappings.get)

    jsonar.asScala.foldLeft(action)((prev, f) => {
      val mapping = f.asInstanceOf[JsonObject]
      val from = mapping.asOption[String]("from")
      val to = mapping.asOption[JsonObject]("to")

      if(from.isDefined) {
        val fromData = from.get match {
          case "request.body" => inAction.body
          case "request.header" => inAction.headers
          case "request.params" => inAction.params
          case "response.body" => input.get.bodyAsString().getOrElse("")
          case "response.header" => input.get.headers()
        }

        val fromFunction = mapping.asOption[JsonObject]("fromFunction")

        // if fromFunction is not defined the from data will be used for the to transformation
        val fromTransformed:String = if(fromFunction.isDefined)
          map(fromFunction.get, fromData) // extracts data defined by from via given functions
        else
          fromData.toString

        val toFunctionRaw = mapping.asOption[JsonObject]("toFunction")

        // if toFunction is defined some transformation rules will be applied to the defined field in "to"
        if(toFunctionRaw.isDefined) {
          // checks if there are any Predefined Placeholders are set and replaces them accordingly
          val toFunction = {
            // replaces the marked ($#from#$) part in the json of toFunction with the transformed from Value e.g. http://www.test.com/something/$#from#$
            var toFunctionLcl = new JsonObject(toFunctionRaw.get.encode().replace(PredefPlaceholder.from, fromTransformed))

            // replaces any date placeholder (\$#date\(.*\)#\$) with the actual date
            toFunctionLcl = new JsonObject(replaceDateFormatToday(toFunctionLcl.encode()))

            toFunctionLcl
          }

          val newAction = to.get.asOption[String]("field").get match {
            case "body" => Some(action.body(map(toFunction, fromTransformed)).asInstanceOf[HttpRequestAction])
            case "url" => Some(action.url(map(toFunction, fromTransformed)).asInstanceOf[HttpRequestAction])
            case "header" => {
              if (to.get.asOption("header").isDefined) {
                val headerField = to.get.asOption[String]("header").get
                val newValue = map(toFunction, fromTransformed)
                Some(action.addHeader(headerField, newValue))
              }
              else None
            }
            case "params" => {
              if (to.get.asOption("param").isDefined) {
                val paramField = to.get.asOption("param").get
                val newValue = map(toFunction, fromTransformed)
                Some(action.addParam(paramField, newValue))
              }
              else None
            }
          }
          newAction.getOrElse(action)
        }
        // if toFunction is not defined the String fromTransformed will be mapped to the defined field (either 1:1 or replaced)
        else {
          to.get.asOption[String]("field") get match {
            case "body" =>
              // gets the body and checks if the PredefPlaceholder is contained if so it will be replaced by the fromTransformed else fromTransformed will be the new body
              if(action.body.contains(PredefPlaceholder.from))
                action.body(action.body.replace(PredefPlaceholder.from, fromTransformed)).asInstanceOf[HttpRequestAction]
              else
                action.body(fromTransformed).asInstanceOf[HttpRequestAction]
            case "url" =>
              // gets the url and checkes if the PredefPlaceholder is contained if so it will be replaced by the fromTransformed else fromTransformed will be the new url
              if(action.url.toString().contains(PredefPlaceholder.from))
                action.url(action.url.toString.replace(PredefPlaceholder.from, fromTransformed)).asInstanceOf[HttpRequestAction]
              else
                action.url(fromTransformed).asInstanceOf[HttpRequestAction]
            case "header" =>
              val headerField = to.get.asOption[String]("header").get

              // gets the set header and checkes if the PredefPlaceholder is contained if so it will be replaced by the fromTransformed else fromTransformed will be the new value for the given header
              if(action.headers.get.getOrElse(headerField, "").contains(PredefPlaceholder.from))
                action.addHeader(headerField, action.headers.get(headerField).replace(PredefPlaceholder.from, fromTransformed))
              else
                action.addHeader(headerField, fromTransformed)
            case "params" =>
              // gets the set param and checkes if the PredefPlaceholder is contained if so it will be replaced by the fromTransformed else fromTransformed will be the new value for the given param
              val paramField = to.get.asOption("param").get

              if(action.params.get.getOrElse(paramField, "").contains(PredefPlaceholder.from))
                action.addParam(paramField, action.params.get(paramField).replace(PredefPlaceholder.from, fromTransformed))
              else
                action.addParam(paramField, fromTransformed)
          }
        }
      }
      else
        action
    })
  }
  else {
    action
  }
}

object HttpResponseMapper {
  def replaceDateFormatToday(s:String):String = {
    val occurrences = PredefPlaceholder.dateNow.regex.findAllIn(s)

    // replaces all occurrences with the actual date and the given foramt
    occurrences.foldLeft(s)((str, occurrence) => {
      // extracts the format surrounded by the Placeholder
      val format = occurrence.replace(PredefPlaceholder.dateNow.postfix, "").replace(PredefPlaceholder.dateNow.prefix, "")
      if(format != "") {
        val df = new SimpleDateFormat(format)
        // applies the date at the moment of execution
        val formattedDate = df.format(new Date())
        str.replace(occurrence, formattedDate)
      }
      else {
        str.replace(occurrence, new Date().getTime.toString)
      }
    })
  }
}

object PredefPlaceholder {
  val from = "$#from#$"
  object dateNow {
    val regex = ("\\$#date\\(([GyYMwWDdFEuaHkKhmsSzZX\\-+_/:., ])*\\)#\\$").r  // matches $#date(<any string>)#$ where <any string> is any kind of string but should be a DateFormat
    val prefix = "$#date("
    val postfix = ")#$"
  }
}
