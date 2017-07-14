package scheduler.workflows.actions

import io.vertx.lang.scala.json.JsonObject
import scheduler.workflows.executors.Executor

/**
  * Created by Sascha on 26.05.2017.
  */
trait AbstractAction[T,R] {
  // will be set by the constructor
  val name:String
  val next:Option[List[AbstractAction[T,R]]]
  val mappings:Option[String]

  // should be set implicit
  val predecessor:Option[AbstractAction[T,R]] // should be set inside the next function. "this" will be the predecessor of the action set as next!
  val executor:Option[Executor[T,R]] // hard coded at the beginning of the constructor
  val actionType:String // hard coded individual name for identifying the Class inside the persistent data (e.g. Json or Xml)

  def predecessor(newPredecessor:AbstractAction[T,R]):AbstractAction[T,R]
  def next(newPredecessor:AbstractAction[T,R]):AbstractAction[T,R]
  def mappings(newMappings:String):AbstractAction[T,R]
  def asJson:JsonObject
  def execute(data:Any):R
}
