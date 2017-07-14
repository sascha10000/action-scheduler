package scheduler.workflows.actions

import java.net.URL

import helper.collections.MapOps
import helper.http.Methods
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient}
import scheduler.workflows.actions.HttpRequestAction.ABSTRACTACTION
import scheduler.workflows.executors.VertxHttpExecutor
import scheduler.workflows.executors.VertxHttpExecutor.VertxRespType

import scala.concurrent.Future

/** Created by Sascha on 26.05.2017.
  */
case class HttpRequestAction(override val name:String,
                             method:Methods.Value,
                             surl:String,
                             mapper:String,
                             body:String = "",
                             client:WebClient,
                             params:Option[Map[String, String]],
                             headers:Option[Map[String, String]],
                             override val mappings:Option[String] = None,
                             override val next:Option[List[AbstractAction[Option[VertxRespType], VertxRespType,  HttpResponse[Buffer]]]] = None,
                             override val predecessor:Option[AbstractAction[Option[VertxRespType], VertxRespType,  HttpResponse[Buffer]]] = None) extends AbstractAction[Option[VertxRespType], VertxRespType,  HttpResponse[Buffer]] {

  val executor = Some(new VertxHttpExecutor(client, Some(this)))

  /** The URL to call */
  val url:URL = new URL(surl)

  override val actionType = HttpRequestAction.actionType

  /** A minimal constructor to create the simplest request.
    *
    *
    * @constructor
   */
  def this(name:String, method:Methods.Value, surl:String, client:WebClient) = this(name, method, surl, "","", client, None, None)

  /** A minimal constructor to create the simplest request including a body.
    *
    * @constructor
    */
  def this(name:String, method:Methods.Value, surl:String, body:String, client:WebClient) = this(name, method, surl, "", body, client, None, None)

  /** Internal constructor for adding a predecessor. Basically a slightly modified copy constructor.
    *
    * @param a the action the predecessor will be added to
    * @param newPredecessor the new predecessor
    * @return the new HttpAction instance with the predecessor
    */
  private def this(a: HttpRequestAction, newPredecessor:ABSTRACTACTION) = this(a.name, a.method, a.surl, a.mapper, a.body, a.client, a.params, a.headers, a.mappings, a.next, Some(newPredecessor))

  /** Adds a new header field to @code headers
    *
    * @param header attribute value pair that will be added to the request's header
    * @return new HttpAction instance with the modified header
    */
  def addHeader(header:(String,String)):HttpRequestAction = HttpRequestAction(name, method, surl, mapper, body, client, params, MapOps.add(this.headers, header), mappings, next, predecessor)

  /** Adds a list of new header fields to @code headers
    *
    * @param headers list of header fields to add to @code headers
    * @return  new HttpAction instance with the modified headers
    */
  def addHeaders(headers:List[(String,String)]):HttpRequestAction = HttpRequestAction(name, method, surl, mapper, body, client, params, MapOps.add(this.headers, headers), mappings, next, predecessor)

  /** Removes a given header field and returns the new instance of HttpAction
    *
    * @param name name of the attribute to remove from the request's header
    * @return new HttpRequestAction instance with the removed attribute
    */
  def removeHeader(name:String):HttpRequestAction = HttpRequestAction(name, method, surl, mapper, body, client, params, MapOps.remove(this.headers, name), mappings, next, predecessor)

  /** Adds a next HttpRequestAction as following actions
    *
    * @param httpAction the new HttpRequestAction to add to next
    * @return the new HttpRequestAction instance containing the added follow-up HttpRequestAction
    */
  def next(httpAction: HttpRequestAction):HttpRequestAction = {
    val newNext = this.next match {
      case None => List(httpAction)
      case Some(actions) => actions :+ httpAction
    }

    HttpRequestAction(name, method, surl, mapper, body, client, params, headers, mappings, Some(newNext), predecessor)
  }

  def next(abstractAction: ABSTRACTACTION):HttpRequestAction = {
    val newNext = this.next match {
      case None => List(abstractAction)
      case Some(actions) => actions :+ abstractAction
    }

    HttpRequestAction(name, method, surl, mapper, body, client, params, headers, mappings, Some(newNext), predecessor)
  }

  /** Adds a new parameter to @code params
    *
    * @param param attribute value pair that will be added to the request's url parameter
    * @return new HttpRequestAction instance with the modified parameters
    */
  def addParam(param:(String, String)):HttpRequestAction = HttpRequestAction(name, method, surl, mapper, body, client, MapOps.add(this.params, param), headers, mappings, next, predecessor)

  /** Adds a list of parameters to @code params
    *
    * @param params parameters to add to @code params
    * @return new HttpRequestAction instance with modified parameters
    */
  def addParams(params:List[(String, String)]):HttpRequestAction = HttpRequestAction(name, method, surl, mapper, body, client, MapOps.add(this.params, params), headers, mappings, next, predecessor)

  /** Removes a given parameter from @code params
    *
    * @param name name of the parameter to remove
    * @return new HttpRequestAction instance with the modified parameters
    */
  def removeParam(name:String):HttpRequestAction = HttpRequestAction(name, method, surl, mapper, body, client, MapOps.remove(this.params, name), headers, mappings, next, predecessor)

  def body(newBody:String):ABSTRACTACTION = HttpRequestAction(name, method, surl, mapper, newBody, client, params, headers, mappings, next, predecessor)

  def url(newUrl:String):ABSTRACTACTION = HttpRequestAction(name, method, newUrl, mapper, body, client, params, headers, mappings, next, predecessor)

  /** Sets a new predecessor
    * THIS METHOD IS ONLY USED INTERNALLY!! IF YOU WANT TO OMIT INCONSISTENCY DON'T USE IT!
    *
    * @param newPredecessor new predecessor
    * @return the HttpRequestAction instance with the new predecessor action
    */
  override def predecessor(newPredecessor:ABSTRACTACTION):HttpRequestAction = new HttpRequestAction(this, newPredecessor)

  override def mappings(newMappings: String): AbstractAction[Option[VertxRespType], VertxRespType,  HttpResponse[Buffer]] = HttpRequestAction(name, method, surl, mapper, body, client, params, headers, Some(newMappings), next, predecessor)

  override def execute(data: Any): VertxRespType = {
    val ret = executor.get.execute(data.asInstanceOf[Option[VertxRespType]])
    next.getOrElse(List()).foreach(_.predecessor(this).execute((Some(ret))))
    ret
  }

  override def asJson:JsonObject = {
    var obj = new JsonObject().put("type",HttpRequestAction.actionType).put("name", this.name).put("method", this.method.toString).put("url", this.surl).put("body", this.body).put("mappings", new JsonArray(mappings.getOrElse("[]")))

    val objParams = this.params.getOrElse(Map()).foldLeft(new JsonArray())((prev, elem) => {
      prev.add(new JsonObject().put("attribute", elem._1).put("value", elem._2))
    })

    val objHeaders = this.headers.getOrElse(Map()).foldLeft(new JsonArray())((prev, elem) => {
      prev.add(new JsonObject().put("attribute", elem._1).put("value", elem._2))
    })

    val objNext = this.next.getOrElse(List()).foldLeft(new JsonArray())((prev, elem) => {
      prev.add(elem.asJson)
    })

    obj = obj.put("params", objParams)
    obj = obj.put("headers", objHeaders)
    obj = obj.put("next", objNext)

    obj
  }
}

object HttpRequestAction {
  /** Typing of the AbstractAction. Used for maintenance reasons. */
  type ABSTRACTACTION = AbstractAction[Option[VertxRespType], VertxRespType, HttpResponse[Buffer]]
  val actionType = "HttpRequestAction"
}
