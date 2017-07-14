package scheduler.workflows.mappers.functions

/**
  * Created by Sascha on 30.05.2017.
  */
abstract class AbstractFunction[T1, T2](in:T1, args:Map[String, T2]) {
  def t():String
}

abstract  class AbstractFunctionObj{
  val functionType:String
  def apply(in:Any, args:Map[String, Any]):AbstractFunction[_,_]
}

