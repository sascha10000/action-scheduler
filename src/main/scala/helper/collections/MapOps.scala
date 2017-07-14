package helper.collections

/**
  * Created by Sascha on 26.05.2017.
  */
object MapOps {
  type OptMap[T1, T2] = Option[Map[T1, T2]]

  /** Adds an 2-tuple to a given map wrapped by an Option
    *
    * @param map map to modify
    * @param add element to add to map
    * @tparam T1 type for the key of the map
    * @tparam T2 type for the value of the map
    * @return the modified map
    */
  def add[T1, T2](map:OptMap[T1, T2], add:(T1, T2)):OptMap[T1, T2] = {
    map match {
      case None => Some(Map(add))
      case Some(m) => Some(m + add)
    }
  }

  /** Adds an list of 2-tuples to a given map nested by an option
    *
    * @param map map to modify
    * @param add elements to add to map
    * @tparam T1 type for the key of the map
    * @tparam T2 type for the value of the map
    * @return the modified map
    */
  def add[T1, T2](map:OptMap[T1, T2], add:List[(T1,T2)]):OptMap[T1, T2] = {
    add.foldLeft(map)((prev, el) => {
      MapOps.add[T1, T2](prev, el)
    })
  }

  /** Removes an element from the map
    *
    * @param map map to modify
    * @param name key to remove from the map
    * @tparam T1 type for the key of the map
    * @tparam T2 type for the value of the map
    * @return the modified map
    */
  def remove[T1, T2](map:OptMap[T1,T2], name:T1):OptMap[T1,T2] = {
    map match {
      case None => None
      case Some(m) => Some(m - name)
    }
  }
}
