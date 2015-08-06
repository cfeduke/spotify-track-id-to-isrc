# Spotify Track ID to ISRC

Simple use of Spray to map a Spotify track ID into its ISRC value.

Usage:

```bash
$ ./sbt console

scala> Spotify.mapTrackIdToISRC("0eGsygTp906u18L0Oimnem")
res0: Option[String] = Some(USIR20400274)
scala> Spotify.mapTrackIdToISRC("nope")
spray.httpx.UnsuccessfulResponseException: Status: 404 Not Found
Body: {
  "error": {
    "status": 404,
    "message": "non existing id"
  }
}
res1: Option[String] = None
```

The `Spotify` object creates an actor system so its not lightweight. If you were to, say, use this in
a Spark job you'd want to combine it with `mapPartitions` and only create one of them per executor.

There is also an `AuthenticatedSpotify` which will one time initialize itself with an authentication
token using your Spotify client ID/client secret for improved rate limiting. It performs no logic for
handling token expiration but with some refactoring it should be do-able using the Akka scheduler or
even standard `try/catch` handling.
