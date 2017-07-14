package scheduler.workflows.mappers

import scheduler.workflows.actions.AbstractAction

/**
  * Created by Sascha on 29.05.2017.
  */
trait AbstractMapper[T, R, P] {
  val inAction:Option[AbstractAction[T,R]]
  val action:AbstractAction[T,R]
  val input: Option[P]

  def exec():AbstractAction[T,R]
}
