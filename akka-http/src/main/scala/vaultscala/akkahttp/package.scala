package vaultscala

import java.net.URI
import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }
import akka.NotUsed
import akka.util.ByteString
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._

package object akkahttp {

  case class AkkaHttpApi()(implicit ac: ActorSystem, mat: Materializer, ec: ExecutionContext) extends Api {
    def to(vault: VaultLocation): ApiRequest = AkkaHttpApiRequestImpl(vault.location)
  }

  private[this] case class AkkaHttpApiRequestImpl(uri: URI, req: HttpRequest = HttpRequest())(implicit ac: ActorSystem, mat: Materializer, ec: ExecutionContext) extends ApiRequest {
    def header(key: String, value: String): ApiRequest = {
      parseHeader(key, value).map { header =>
        AkkaHttpApiRequestImpl(uri, req.copy(headers = req.headers :+ header))
      }.getOrElse(this)
    }
    def get(path: String): Future[Try[SuccessResponse]] = {
      val request = req.copy(uri = path)
      responseFuture(request).flatMap {
        case (Success(HttpResponse(OK, _, entity, _)), _) => {
          entireEntity(entity).map(ee => Success(SuccessResponse(ee)))
        }
        case (Success(HttpResponse(badStatus, _, entity, _)), _) => {
          entireEntity(entity).map(ee => Failure(OperationError(ee, badStatus.intValue)))
        }
        case (Failure(t), _) => Future.successful(Failure(t))
      }
    }
    private[this] def responseFuture(request: HttpRequest): Future[(Try[HttpResponse], NotUsed)] = {
      Source.single(request -> NotUsed)
        .via(poolClientFlow)
        .runWith(Sink.head)
    }
    private[this] lazy val poolClientFlow = {
      val (httpPort, httpsPort) = if (uri.getPort > 0) (uri.getPort, uri.getPort) else (80, 443)
      uri.getScheme match {
        case "http" => Http().cachedHostConnectionPool[NotUsed](uri.getHost, httpPort)
        case "https" => Http().cachedHostConnectionPoolHttps[NotUsed](uri.getHost, httpsPort)
      }
    }
    private[this] def entireEntity(entity: HttpEntity) = entity.dataBytes.runFold(ByteString.empty) { case (acc, b) => acc ++ b }.map(_.decodeString("UTF-8"))
    private[this] def parseHeader(key: String, value: String): Option[HttpHeader] = {
      HttpHeader.parse(key, value) match {
        case HttpHeader.ParsingResult.Ok(header, _) => Some(header)
        case HttpHeader.ParsingResult.Error(_) => None
      }
    }
  }

  implicit def akkaHttpApi(implicit ac: ActorSystem, mat: Materializer, ec: ExecutionContext): Api = AkkaHttpApi()

}
