import helper.collections.MapOps
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Sascha on 26.05.2017.
  */
class MapOpsSpec extends FlatSpec with Matchers {
  val map = Some(Map("a" -> "test1", "b" -> "test2"))

  "Adding" should "add a value to the given map" in {
    val nMap = MapOps.add(map, ("c" -> "test3"))
    nMap.get.contains("c") should be (true)
  }

  "Adding multiple" should "add a list of values to the given map" in {
    val add = List(("d" -> "test4"), ("e" -> "test5"))
    val nMap = MapOps.add(map, add)
    nMap.get.contains("d") should be (true)
    nMap.get.contains("e") should be (true)
  }

  "Removing" should "remove an given key from a given map" in {
    val nMap = MapOps.remove(map, "a")
    nMap.contains("a") should be (false)
  }
}
