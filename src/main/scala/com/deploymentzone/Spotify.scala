package com.deploymentzone

import akka.actor.ActorSystem
import com.typesafe.scalalogging.slf4j.LazyLogging
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

  def requestResponse(request: HttpRequest): Future[HttpResponse] = {
    val pipeline = (sendReceive
      ~> decode(Deflate))
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

  def requestResponse(request: HttpRequest): Future[HttpResponse] = {
    val pipeline = (addHeader("Authorization", s"Bearer $accessToken")
      ~> sendReceive
      ~> decode(Deflate))
    pipeline(request)
  }
}

trait Service {

  implicit val system = ActorSystem()

  def request[A: FromResponseUnmarshaller](request: HttpRequest): Future[A]

  def requestResponse(request: HttpRequest): Future[HttpResponse]
}

trait RetrieveISRCForTrack extends Service with LazyLogging {

  import system.dispatcher

  def apply(trackId: String): Option[String] = {
    val future = (for {
      track <- request[Track](Get(s"https://api.spotify.com/v1/tracks/$trackId"))
    } yield track.externalIds.isrc).recoverWith {
      case ex =>
        logger.error("Exception", ex)
        Future.successful(None)
    }
    Await.result(future, 20.seconds)
  }
}

trait RetrieveISRCForTracks extends Service with LazyLogging {

  import system.dispatcher

  private val empty = Seq.empty[(String, String)]

  def apply(trackIds: Seq[String]): Seq[(String, String)] = {
    assert(trackIds.length <= 50, "Spotify API accepts no more than 50 track IDs at a time")

    var results = empty

    do {
      val future = requestResponse(Get(s"https://api.spotify.com/v1/tracks/?ids=${trackIds.mkString(",")}"))
        .map { response => response.status.intValue match {
        case 429 =>
          val wait = response.headers.find(_.lowercaseName == "retry-after").map(_.value.toInt + 1).getOrElse(60)
          logger.error(s"Rate limited; retrying after $wait seconds")
          Thread.sleep(wait.seconds.toMillis)
          empty
        case 200 =>
          import spray.httpx.unmarshalling._

          response.entity.as[Tracks] match {
            case Right(tracks) =>
              tracks.tracks.zip(trackIds).map { case (t, trackId) =>
                (trackId, t.map(_.externalIds.isrc.getOrElse("")).getOrElse(""))
              }
            case Left(de) =>
              logger.error("Deserialization error; exiting", de)
              println(de)
              System.exit(1)
              empty
          }
        case code =>
          val wait = 10.seconds
          logger.error(s"Encountered an unexpected HTTP status code $code, waiting ${wait.toSeconds} seconds and retrying")
          Thread.sleep(wait.toMillis)
          empty
      }
      }.recoverWith {
        case ex =>
          logger.error("Exception: ", ex)
          Future.successful(empty)
      }
      results = Await.result(future, 20.seconds)
    } while (results.isEmpty)

    results
  }
}
