import helper.Ops
import helper.http.Methods
import helper.test.JSONData
import io.vertx.core.json.JsonObject
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.WebClient
import org.scalatest.{FlatSpec, Matchers}
import parser.ActionJsonParser
import scheduler.workflows.actions.{HttpRequestAction, PrintResponseAction}

/**
  * Created by Sascha on 28.05.2017.
  */
class AJParserSpec extends FlatSpec with Matchers {
  val client = WebClient.create(Vertx.vertx())
  val json1 = "{\"name\":\"Max\",\"knows\":[],\"items\":[]}"
  val getaction = new HttpRequestAction("Get Parsed Action", Methods.GET, "http://localhost:3000/persons", client)
  val postaction = new HttpRequestAction("Post Parsed Action", Methods.POST, "http://localhost:3000/person", json1 ,client)
  val printaction = new PrintResponseAction("Print Parsed Action")

  val run = false

  behavior of "The Parser"

  if(run) {it} else {ignore} should " convert generated (!) JSON to according Actions and execute them" in {
    // a test case
    val actionChain = postaction.addHeader("TEST1", "TEST1").addHeaders(List(("TEST2", "TEST2"))).addParam("TEST3", "TEST3").addParams(List(("TEST4", "TEST4"))).next(printaction.next(getaction.next(printaction)))
    val actJson = actionChain.asJson

    // convert to a string
    val strJson = new JsonObject(actJson.encode())

    Ops.condense(strJson.encode()) should be (Ops.condense(JSONData.ActionChain.withoutMapping))

    // convert
    val chain = new ActionJsonParser(strJson).action

    chain.actionType should be (HttpRequestAction.actionType)
    chain.next.get(0).actionType should be (PrintResponseAction.actionType)

    chain.execute(None)
  }

  if(run) {it} else {ignore} should "convert given JSON with some missing optional fields to a single Action and execute it" in {
    val action = new ActionJsonParser(new JsonObject(JSONData.Action.missingArrays)).action

    action.actionType should be (HttpRequestAction.actionType)
    action.predecessor should be (None)
    action.next should be (None)

    action.execute(None)
  }

  if(run) {it} else {ignore} should "convert given JSON with some empty optional fields to a single Action and execute it" in {
    val action = new ActionJsonParser(new JsonObject(JSONData.Action.emptyArrays)).action

    action.actionType should be (HttpRequestAction.actionType)
    action.predecessor should be (None)
    action.next should be (None)

    action.execute(None)
  }

  if(run) {it} else {ignore} should "convert given JSON with a mapping to an action and execute it" in {

  }
}
