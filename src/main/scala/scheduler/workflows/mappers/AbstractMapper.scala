package scheduler.workflows.mappers

import helper.json.JSON._
import io.vertx.lang.scala.json.JsonObject
import scheduler.workflows.actions.AbstractAction
import scheduler.workflows.mappers.functions._

/**
  * Created by Sascha on 29.05.2017.
  */
abstract class AbstractMapper[T, R, P](action:AbstractAction[T, R, P]) {

  val inAction:Option[AbstractAction[T,R, P]] = resolvePredecessor(action)
  val input: Option[P]

  def exec():AbstractAction[T,R, P]
  def resolvePredecessor(action:AbstractAction[T, R, P]):Option[AbstractAction[T, R, P]]

  protected def map[T](json:JsonObject, input:T):String = {
    val nextInput = (json.getString("type") match {
      case JsonExtractor.functionType => JsonExtractor(input.asInstanceOf[String], json.attributesAsMap())
      case MapExtractor.functionType => new MapExtractor(input.asInstanceOf[Map[String, String]], json.attributesAsMap())
      case PlaceholderReplacer.functionType => PlaceholderReplacer(input.asInstanceOf[String], json.attributesAsMap())
      case StringReplacer.functionType => StringReplacer(input.asInstanceOf[String], json.attributesAsMap())
      case JsonMerger.functionType => JsonMerger(input.asInstanceOf[String], json.attributesAsMap())
      case JsonFieldReplacer.functionType => JsonFieldReplacer(input.asInstanceOf[String], json.attributesAsMap())
      case JsonArrayResetter.functionType => JsonArrayResetter(input.asInstanceOf[String], json.attributesAsMap())
    }).t()

    val next = json.asOption[JsonObject]("function")

    if(next.isEmpty)
      nextInput
    else {
      map(next.get, nextInput)
    }
  }
}
