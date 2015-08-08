package com.deploymentzone

import com.scalapenos.spray.SnakifiedSprayJsonSupport
import spray.json.DefaultJsonProtocol

object SpotifyProtocol extends DefaultJsonProtocol with SnakifiedSprayJsonSupport {

  implicit val externalIdsProtocol = jsonFormat1(ExternalIds)
  implicit val trackProtocol = jsonFormat1(Track)
  implicit val tracksProtocol = jsonFormat1(Tracks)
  implicit val accessTokenProtocol = jsonFormat3(AccessToken)

}

case class ExternalIds(isrc: String)

case class Track(externalIds: ExternalIds)

case class Tracks(tracks: Seq[Option[Track]])

case class AccessToken(accessToken: String, tokenType: String, expiresIn: Int)

/* track id: 0eGsygTp906u18L0Oimnem
{
  "album" : {
    "album_type" : "album",
    "available_markets" : [ "AD", "AR", "AT", "AU", "BE", "BG", "BO", "BR", "CH", "CL", "CO", "CR", "CY", "CZ", "DE", "DK", "DO", "EC", "EE", "ES", "FI", "FR", "GR", "GT", "HK", "HN", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MT", "MY", "NI", "NL", "NO", "NZ", "PA", "PE", "PH", "PL", "PT", "PY", "RO", "SE", "SG", "SI", "SK", "SV", "TR", "TW", "UY" ],
    "external_urls" : {
      "spotify" : "https://open.spotify.com/album/6TJmQnO44YE5BtTxH8pop1"
    },
    "href" : "https://api.spotify.com/v1/albums/6TJmQnO44YE5BtTxH8pop1",
    "id" : "6TJmQnO44YE5BtTxH8pop1",
    "images" : [ {
      "height" : 640,
      "url" : "https://i.scdn.co/image/8e13218039f81b000553e25522a7f0d7a0600f2e",
      "width" : 629
    }, {
      "height" : 300,
      "url" : "https://i.scdn.co/image/8c1e066b5d1045038437d92815d49987f519e44f",
      "width" : 295
    }, {
      "height" : 64,
      "url" : "https://i.scdn.co/image/d49268a8fc0768084f4750cf1647709e89a27172",
      "width" : 63
    } ],
    "name" : "Hot Fuss",
    "type" : "album",
    "uri" : "spotify:album:6TJmQnO44YE5BtTxH8pop1"
  },
  "artists" : [ {
    "external_urls" : {
      "spotify" : "https://open.spotify.com/artist/0C0XlULifJtAgn6ZNCW2eu"
    },
    "href" : "https://api.spotify.com/v1/artists/0C0XlULifJtAgn6ZNCW2eu",
    "id" : "0C0XlULifJtAgn6ZNCW2eu",
    "name" : "The Killers",
    "type" : "artist",
    "uri" : "spotify:artist:0C0XlULifJtAgn6ZNCW2eu"
  } ],
  "available_markets" : [ "AD", "AR", "AT", "AU", "BE", "BG", "BO", "BR", "CH", "CL", "CO", "CR", "CY", "CZ", "DE", "DK", "DO", "EC", "EE", "ES", "FI", "FR", "GR", "GT", "HK", "HN", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MT", "MY", "NI", "NL", "NO", "NZ", "PA", "PE", "PH", "PL", "PT", "PY", "RO", "SE", "SG", "SI", "SK", "SV", "TR", "TW", "UY" ],
  "disc_number" : 1,
  "duration_ms" : 222075,
  "explicit" : false,
  "external_ids" : {
    "isrc" : "USIR20400274"
  },
  "external_urls" : {
    "spotify" : "https://open.spotify.com/track/0eGsygTp906u18L0Oimnem"
  },
  "href" : "https://api.spotify.com/v1/tracks/0eGsygTp906u18L0Oimnem",
  "id" : "0eGsygTp906u18L0Oimnem",
  "name" : "Mr. Brightside",
  "popularity" : 75,
  "preview_url" : "https://p.scdn.co/mp3-preview/f454c8224828e21fa146af84916fd22cb89cedc6",
  "track_number" : 2,
  "type" : "track",
  "uri" : "spotify:track:0eGsygTp906u18L0Oimnem"
}
 */
