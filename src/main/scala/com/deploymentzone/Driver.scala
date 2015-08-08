package com.deploymentzone

import scala.concurrent.duration.Duration
import scala.io.Source
import scala.concurrent.duration._

object Driver extends App {

  val (clientId, clientSecret) = (args(1), args(2))

  def exponentialBackOff(r: Int): Duration = scala.math.pow(2, scala.math.min(r,8)).round * 5.seconds

  Source.fromFile(args(3)).getLines().grouped(50).foreach { trackIds =>
    var execute = true
    var attempt = 1
    while(execute) {
      execute = false
      val results = AuthenticatedSpotifyTracksISRC(clientId, clientSecret)(trackIds)
      if (results.forall { case (_, isrc) => isrc.length == 0 }) {
        Thread.sleep(exponentialBackOff(attempt).toMillis)
        execute = true
        attempt += 1
      } else {
        results.foreach { case (trackId, isrc) => println(s"$trackId,$isrc") }
        Thread.sleep(1000)
      }
    }
  }

}
