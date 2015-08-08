package com.deploymentzone

import java.io._

import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.io.Source
import scala.concurrent.duration._
import scala.util.control.Exception.ultimately

object Driver extends App with LazyLogging {

  val (clientId, clientSecret) = (args(1), args(2))
  val inputFile = new File(args(3))
  val outputFile = new File(args.lastOption.getOrElse("output.txt"))

  private val lines: Iterator[String] = Source.fromFile(inputFile).getLines()

  var input: Iterator[String] = Seq.empty[String].iterator
  if (outputFile.exists) {
    val lastLine = Source.fromFile(outputFile).getLines().foldLeft("") { case (_, str) => str }
    if (lastLine.length > 0) {
      val lastTrackId = lastLine.split(",").head
      logger.info(s"Skipping to track $lastTrackId")
      input = Source.fromFile(inputFile).getLines().dropWhile(trackId => trackId != lastTrackId)
      if (input.hasNext) input.next()
    }
  }
  if (input.isEmpty) {
    input = Source.fromFile(inputFile).getLines()
  }

  val writer = new FileWriter(outputFile, outputFile.exists())
  ultimately(writer.close()) {
    input.grouped(50).foreach { trackIds =>
      logger.info(s"Processing ${trackIds.length} records")
      AuthenticatedSpotifyTracksISRC(clientId, clientSecret)(trackIds).foreach { case (trackId, isrc) =>
        writer.write(s"$trackId,$isrc\n")
      }
      Thread.sleep(2.seconds.toMillis)
    }
  }

}
