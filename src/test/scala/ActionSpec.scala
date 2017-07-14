import helper.http.Methods
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.WebClient
import org.scalatest.{FlatSpec, Matchers}
import scheduler.workflows.actions.{HttpRequestAction, PrintResponseAction}

import scala.concurrent.Await

/**
  * Created by Sascha on 26.05.2017.
  */
class ActionSpec extends FlatSpec with Matchers {
  import scala.concurrent.duration._

  val vertx:Vertx = Vertx.vertx()
  val client = WebClient.create(vertx)

  val json1 = "{\"name\":\"Max\",\"knows\":[],\"items\":[]}"
  val json2 = "{\"name\":\"Mustermann\",\"knows\":[],\"items\":[]}"
  val getaction = new HttpRequestAction("Get Action", Methods.GET, "http://localhost:3000/persons", client)
  val postaction = new HttpRequestAction("Post Action", Methods.POST, "http://localhost:3000/person", json1 ,client)
  val printaction = new PrintResponseAction("Print Action")

  val run = false

  behavior of "The Action/s"

  if(run) {it} else {ignore} should "POST a JSON Object" in {
    val resp = postaction.next(printaction).execute(None)
    val respdata = Await.result(resp, 10 seconds).bodyAsString()
    respdata.getOrElse("") should include (json1)
  }

  if(run) {it} else {ignore} should "GET a JsonArray of Persons" in {
    val resp = getaction.next(printaction).execute(None)
    val respdata = Await.result(resp, 10 seconds).bodyAsString()

    respdata.getOrElse("") should include (json1)
  }

  if(run) {it} else {ignore} should "first POST and then GET the data" in {
    val action = postaction.body(json2).next(printaction.next(getaction.next(printaction)))
    val resp = action.execute(None)
    val respdata = Await.result(resp, 10 seconds).bodyAsString()

    respdata.getOrElse("") should include (json2)
  }

  if(run) {it} else {ignore} should "first POST and then GET only the posted Data via a Mapping" in {
    val getpersonaction = new HttpRequestAction("Get Person Action", Methods.GET, "http://localhost:3000/person/${name}", client)
    val postToGetMapping = "[{\n  \"from\" : \"response.body\", \"to\": { \"field\": \"url\" },\n  \"fromFunction\":{\n    \"type\" : \"JsonExtractor\",\n    \"jsonp\": \"$.name\"\n  },\n  \"toFunction\": {\n    \"type\" : \"PlaceholderReplacer\",\n    \"replace\": \"name\",\n    \"replaceWith\" : \"$#from#$\"\n  }\n}]"
    val resp = postaction.body(json2).next(printaction.next(getpersonaction.mappings(postToGetMapping).next(printaction))).execute(None)

    val respdata = Await.result(resp, 10 seconds).bodyAsString()
  }
}
