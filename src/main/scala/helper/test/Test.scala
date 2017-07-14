package helper.test

import scheduler.workflows.actions.HttpRequestAction.ABSTRACTACTION

/**
  * Created by Sascha on 02.06.2017.
  */
object Test {
  def printActionChain(i:ABSTRACTACTION):Unit = {
    println(i)

    if(i.next.isDefined)
      i.next.get.foreach(f => {
        printActionChain(f)
      })
    else
      println("END")
  }
}
