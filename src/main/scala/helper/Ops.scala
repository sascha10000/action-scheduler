package helper

/**
  * Created by Sascha on 30.05.2017.
  */
object Ops {
  /** Checks if a value is null and returns the default value if true.
    *
    * @param value the value to be checked against 0
    * @param default the default returned if value is null
    * @tparam T the type used for value and default
    * @return returns value or default if value is null
    */
  def getOrElse[T](value:T, default:T): T ={
    if(value == null) default
    else value
  }

  /** Returns an Option. It is possible to apply an additional function for conversion from T1 -> T2
    *
    * @param value the value to convert to an option
    * @param fun a function that will be applied to value if it is not null. As default it's converting value to T2.
    * @tparam T1 input type of value
    * @tparam T2 return type Option[T2]
    * @return returns an Option regarding to parameter value
    */
  def asOption[T1, T2](value:T1, fun:(T1) => (T2) = (n:T1) => { n.asInstanceOf[T2] }):Option[T2] = {
    if(value == null) None
    else Option(fun(value))
  }

  /** Compares to strings after condensing them.
    *
    * @param a first string to compare
    * @param b second string to compare
    * @return true if a==b else false
    */
  def compCondensed(a:String, b:String) = {
    condense(a) == condense(b)
  }

  /** Removes several chars of a given string. (e.g. linefeed, carriage-return, tab)
    *
    * @param str string to condense
    * @return condensed string
    */
  def condense(str:String) = str.replace("\n", "").replace(" ", "").replace("\t", "")

  /** Executes a given function if the provided value is not None. Else it returns None.
    *
    * @param value the value which may be None
    * @param func the function to execute whether value is defined or not
    * @tparam T1 type of the value option
    * @tparam T2 type of the return parameter
    * @return
    */
  def ifNotNone[T1, T2](value:Option[T1], func:() => T2):Option[T2] = {
    if(value.isDefined)
      Some(func())
    else None
  }
}
