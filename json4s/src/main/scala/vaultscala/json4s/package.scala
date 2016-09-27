package vaultscala

import vaultscala._
import java.net.URI
import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }
import org.json4s._, jackson.JsonMethods._

package object json4s {

  class Json4sResponseExtractor()(implicit s: Serialization = jackson.Serialization, f: Formats = jackson.Serialization.formats(NoTypeHints)) extends ResponseExtractor {
    def extract[T](response: SuccessResponse, key: String)(implicit m: Manifest[T]): T = {
      val json = parse(response.entity)
      key.split('/').foldLeft(json)((j, k) => j \ k).extract[T]
    }
  }

  implicit def json4sExtractor(implicit s: Serialization = jackson.Serialization, f: Formats = jackson.Serialization.formats(NoTypeHints)): ResponseExtractor = new Json4sResponseExtractor()

}
