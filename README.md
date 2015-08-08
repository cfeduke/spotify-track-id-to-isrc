# Spotify Track ID to ISRC

Simple use of Spray to map a Spotify track ID into its ISRC value.

Usage:

```bash
$ ./sbt console

scala> val mapTrackToISRC = StandardPipeline with RetrieveISRCForTrack

scala> mapTrackToISRC("0eGsygTp906u18L0Oimnem")
res0: Option[String] = Some(USIR20400274)
scala> mapTrackToISRC("nope")
[ERROR] spray.httpx.UnsuccessfulResponseException: Status: 404 Not Found
[ERROR] Body: {
  "error": {
    "status": 404,
    "message": "non existing id"
  }
}
res1: Option[String] = None
```

The underlying service class creates an actor system so its not lightweight. If you were to, say, use this in
a Spark job you'd want to combine it with `mapPartitions` and only create one of them per executor.

There is also an `AuthenticatedSpotify` which will one time initialize itself with an authentication
token using your Spotify client ID/client secret for improved rate limiting. It performs no logic for
handling token expiration but with some refactoring it should be do-able using the Akka scheduler or
even standard `try/catch` handling.

Finally there is also a `RetrieveISRCForTracks` function object which handles 429 pauses and processes track IDs
at a rate of 50 per request. See the `Driver.scala` for an example implementation that works from an input file.
