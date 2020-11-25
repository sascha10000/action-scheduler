package helper

import io.vertx.core.json.JsonObject
import io.vertx.scala.ext.web.RoutingContext
import io.vertx.scala.ext.web.client.WebClient
import server.Config

import scala.concurrent.{ExecutionContext, Future}

class AuthHelper(val httpClient:WebClient)(implicit val ec:ExecutionContext) {
  def forClientInChecklist(rc:RoutingContext, checklist:JsonObject):Future[Either[String, Int]] = {
    authorization(rc, checklist){
      (checklist, rc, token) => {
        val clientId = checklist.getString("clientId")
        httpClient.get(Config.aaservice.port, Config.aaservice.host,
          "/authorized/GET/on/clients/with/"+clientId+"/by/checklist")
          .putHeader("Authorization", token)
          .sendFuture() flatMap { resp =>
          Future { Right(resp.statusCode()) }
        }
      }
    }
  }

  def findAuthorized(objToAuthorize:String, rc:RoutingContext): Future[Either[String, Int]] = {
    authorizationNoData(rc){
      (rc, token) => {
        httpClient.get(Config.aaservice.port, Config.aaservice.host, "/authorized/GET/on/"+objToAuthorize+"/by/checklist")
          .putHeader("Authorization", token)
          .sendFuture() flatMap { resp =>
            Future {
              resp.statusCode() match {
                case 200 => Left(resp.bodyAsString().get)
                case _ => Right(resp.statusCode())
              }
            }
          }
      }
    }
  }

  def authorization(rc:RoutingContext, relevantObject:JsonObject)(onSuccess:(JsonObject, RoutingContext, String) => Future[Either[String, Int]] , onError:() => Future[Either[String, Int]] = () => Future { Left("{ \"msg\": \"Missing Authorization Header\"}") }): Future[Either[String, Int]] = {
    rc.request().getHeader("Authorization") match {
      case Some(token) => onSuccess(relevantObject, rc, token)
      case None => onError()
    }
  }

  def authorizationNoData(rc:RoutingContext)(onSuccess:(RoutingContext, String) => Future[Either[String, Int]] , onError:() => Future[Either[String, Int]] = () => Future { Left("{ \"msg\": \"Missing Authorization Header\"}") }): Future[Either[String, Int]] = {
    rc.request().getHeader("Authorization") match {
      case Some(token) => onSuccess(rc, token)
      case None => onError()
    }
  }
}
