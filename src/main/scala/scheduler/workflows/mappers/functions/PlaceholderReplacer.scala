package scheduler.workflows.mappers.functions

/**
  * Created by Sascha on 30.05.2017.
  */
// If left with defaults the t function will search for "${ replaceWith }" and replace it including the quotes!
class PlaceholderReplacer(in:String, args:Map[String, String], start:String = "${", end:String = "}") extends StringReplacer(in, args + (("replace", start + args.getOrElse("replace", "") + end))) {
  override def t():String = super.t()
}

object PlaceholderReplacer extends AbstractFunctionObj {
  val functionType = "PlaceholderReplacer"

  override def apply(in: Any, args: Map[String, Any]): AbstractFunction[_, _] = new PlaceholderReplacer(in.asInstanceOf[String], args.asInstanceOf[Map[String, String]])
}
