package webclients

import java.net.URL

import scala.concurrent.Future

/**
  * Created by Sascha on 27.05.2017.
  */
abstract trait AbstractWebClient {
  def get(url:URL, body:String, params:Map[String, String] = Map(), headers:Map[String, String] = Map())
}
