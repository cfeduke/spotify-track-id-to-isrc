package com.deploymentzone

import scala.io.Source

object Driver extends App {

  val (clientId, clientSecret) = (args(1), args(2))

  Source.fromFile(args(3)).getLines().grouped(50).foreach { trackIds =>
    var execute = true
    while(execute) {
      execute = false
      val results = AuthenticatedSpotifyTracksISRC(clientId, clientSecret)(trackIds)
      if (results.forall { case (_, isrc) => isrc.length == 0 }) {
        Thread.sleep(20000)
        execute = true
      }
      results.foreach { case (trackId, isrc) => println(s"$trackId,$isrc") }
      Thread.sleep(1000)
    }
  }

}
