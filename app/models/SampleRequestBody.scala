package models

import play.api.libs.json.{Format, Json}

case class SampleRequestBody(key1: String, key2: String)

object SampleRequestBody {
  implicit val playJsonSampleRequestBodyFormat: Format[SampleRequestBody] = Json.format[SampleRequestBody]
}
