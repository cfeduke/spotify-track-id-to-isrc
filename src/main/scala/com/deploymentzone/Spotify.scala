package com.deploymentzone

import akka.actor.ActorSystem
import org.parboiled.common.Base64
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.httpx.encoding.Deflate

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object Spotify extends Service {

  import SpotifyProtocol._

  import system.dispatcher

  val pipeline: HttpRequest => Future[Track] = (
    sendReceive
      ~> decode(Deflate)
      ~> unmarshal[Track])

}

case class AuthenticatedSpotify(clientId: String, clientSecret: String) extends Service {
  import SpotifyProtocol._

  import system.dispatcher

  private val base64ClientIdSecret = Base64.rfc2045().encodeToString(s"$clientId:$clientSecret".getBytes, false)

  private val authenticationPipeline: HttpRequest => Future[AccessToken] = (
    addHeader("Authorization", s"Basic $base64ClientIdSecret")
    ~> sendReceive
    ~> decode(Deflate)
    ~> unmarshal[AccessToken]
    )

  private val applicationXWwwFormUrlEncoded: ContentType = ContentType(MediaType.custom("application/x-www-form-urlencoded"))

  lazy val accessToken =
    Await.result(authenticationPipeline(Post("https://accounts.spotify.com/api/token")
      .withEntity(HttpEntity(applicationXWwwFormUrlEncoded, "grant_type=client_credentials"))), 20.seconds)
      .accessToken

  lazy val pipeline: HttpRequest => Future[Track] = (
    addHeader("Authorization", s"Bearer $accessToken")
    ~> sendReceive
    ~> decode(Deflate)
    ~> unmarshal[Track]
    )
}

trait Service {

  implicit val system = ActorSystem()
  import system.dispatcher

  def pipeline: HttpRequest => Future[Track]

  def mapTrackIdToISRC(trackId: String): Option[String] = {
    val future = (for {
      track <- pipeline(Get(s"https://api.spotify.com/v1/tracks/$trackId"))
    } yield Option(track.externalIds.isrc)).recoverWith {
      case ex =>
        println(ex)
        Future.successful(None)
    }
    Await.result(future, 20.seconds)
  }
}
