package scheduler.workflows.executors

/**
  * Created by Sascha on 26.05.2017.
  */
trait Executor[T, R] {
  def execute(data:T):R
}
