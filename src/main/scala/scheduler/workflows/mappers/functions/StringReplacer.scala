package scheduler.workflows.mappers.functions

/**
  * Created by Sascha on 30.05.2017.
  */
class StringReplacer(in:String, args:Map[String, String]) extends AbstractFunction[String, String](in, args) {
  def t():String = {
    in.replace(args.getOrElse("replace", ""), args.getOrElse("replaceWith", ""))
  }
}

object StringReplacer extends AbstractFunctionObj {
  val functionType = "StringReplacer"
  override def apply(in: Any, args: Map[String, Any]): AbstractFunction[_, _] = new StringReplacer(in.asInstanceOf[String], args.asInstanceOf[Map[String, String]])
}
