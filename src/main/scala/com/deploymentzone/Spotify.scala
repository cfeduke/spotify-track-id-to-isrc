package com.deploymentzone

import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.httpx.encoding.Deflate

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object Spotify {

  import SpotifyProtocol._

  implicit val system = ActorSystem()

  import system.dispatcher

  val pipeline: HttpRequest => Future[Track] = (
    sendReceive
      ~> decode(Deflate)
      ~> unmarshal[Track])

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
