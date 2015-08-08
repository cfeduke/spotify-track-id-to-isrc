package com.deploymentzone

import akka.actor.ActorSystem
import org.parboiled.common.Base64
import spray.client.pipelining._
import spray.http._
import spray.httpx.encoding.Deflate
import spray.httpx.unmarshalling.FromResponseUnmarshaller

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import spray.httpx.SprayJsonSupport._
import SpotifyProtocol._


case class AuthenticatedSpotifyTrackISRC(clientId: String, clientSecret: String)
  extends AuthenticatedPipeline
  with RetrieveISRCForTrack

case class AuthenticatedSpotifyTracksISRC(clientId: String, clientSecret: String)
  extends AuthenticatedPipeline
  with RetrieveISRCForTracks

class StandardPipeline extends Service {

  import system.dispatcher

  def request[A: FromResponseUnmarshaller](request: HttpRequest): Future[A] = {
    val pipeline = (sendReceive
      ~> decode(Deflate)
      ~> unmarshal[A])
    pipeline(request)
  }
}

trait AuthenticatedPipeline extends Service {
  def clientId: String

  def clientSecret: String

  import SpotifyProtocol._
  import spray.httpx.SprayJsonSupport._
  import system.dispatcher

  private val base64ClientIdSecret = Base64.rfc2045().encodeToString(s"$clientId:$clientSecret".getBytes, false)

  protected val authenticationPipeline: HttpRequest => Future[AccessToken] = (
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

  def request[A: FromResponseUnmarshaller](request: HttpRequest): Future[A] = {
    val pipeline = (addHeader("Authorization", s"Bearer $accessToken")
      ~> sendReceive
      ~> decode(Deflate)
      ~> unmarshal[A])
    pipeline(request)
  }
}

trait Service {

  implicit val system = ActorSystem()

  def request[A: FromResponseUnmarshaller](request: HttpRequest): Future[A]
}

trait RetrieveISRCForTrack extends Service {
  import system.dispatcher

  def apply(trackId: String): Option[String] = {
    val future = (for {
      track <- request[Track](Get(s"https://api.spotify.com/v1/tracks/$trackId"))
    } yield Option(track.externalIds.isrc)).recoverWith {
      case ex =>
        println(ex)
        Future.successful(None)
    }
    Await.result(future, 20.seconds)
  }
}

trait RetrieveISRCForTracks extends Service {
  import system.dispatcher

  def apply(trackIds: Seq[String]): Seq[(String, String)] = {
    assert(trackIds.length <= 50, "Spotify API accepts no more than 50 track IDs at a time")

    val future = (for {
      tracks <- request[Tracks](Get(s"https://api.spotify.com/v1/tracks/?ids=${trackIds.mkString(",")}"))
    } yield tracks.tracks.zip(trackIds).map { case (t, trackId) => (trackId, t.map(_.externalIds.isrc).getOrElse("")) })
      .recoverWith {
      case ex =>
        println(ex)
        Future.successful(trackIds.map { trackId => (trackId, "") })
    }
    Await.result(future, 20.seconds)
  }
}
