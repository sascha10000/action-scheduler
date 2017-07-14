package scheduler.workflows.mappers.functions

/**
  * Created by Sascha on 30.05.2017.
  */
class MapExtractor[K,V](in:Map[K, V], args:Map[String, Any]) extends AbstractFunction[Map[K,V], Any](in, args) {
  def t():String = {
    val find:K = args("find").asInstanceOf[K]
    val default:V = args("default").asInstanceOf[V]

    in.getOrElse(find, default).asInstanceOf[String]
  }
}

object MapExtractor  {
  val functionType = "MapExtractor"
}
