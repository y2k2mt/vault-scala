package object vaultscala {

  import java.net.URI
  import scala.concurrent.{ Future, Await, ExecutionContext }
  import scala.concurrent.duration._
  import scala.util.{ Try, Success, Failure }

  case class VaultLocation(location: URI)

  case class OperationError(reason: String, status: Int) extends Exception
  case class SuccessResponse(entity: String, status: Int = 200)

  trait ApiRequest {
    def header(key: String, value: String): ApiRequest
    def get(path: String): Future[Try[SuccessResponse]]
  }

  trait Api {
    def to(vault: VaultLocation): ApiRequest
  }

  trait Operation[I, O] {
    def op(vault: VaultLocation, param: I, ac: Api, re: ResponseExtractor)(implicit ec: ExecutionContext): Future[Try[O]]
  }

  trait ResponseExtractor {
    def extract[T](response: SuccessResponse, key: String)(implicit m: Manifest[T]): T
  }

  object Vault {
    def apply[T, F](data: T)(implicit op: Operation[T, F], api: Api, re: ResponseExtractor, ec: ExecutionContext, vault:VaultLocation): Future[Try[F]] = op.op(vault, data, api, re)
    def sync[T, F](data: T)(implicit op: Operation[T, F], api: Api, re: ResponseExtractor, ec: ExecutionContext,vault:VaultLocation): Try[F] = {
      Await.result(apply(data), Duration.Inf)
    }
  }

  case class ClientToken(token: String)
  case class SingleSecret(token: ClientToken, key: String)
  case class SingleSecretValue(value: String)
  case class SecretList(token: ClientToken)
  case class SecretListValue(values: Seq[String])

  object SingleSecretOperation extends Operation[SingleSecret, SingleSecretValue] {
    def op(vault: VaultLocation, key: SingleSecret, api: Api, re: ResponseExtractor)(implicit ec: ExecutionContext): Future[Try[SingleSecretValue]] = {
      val Path = s"/v1/secret/${key.key}"
      api.to(vault).header("X-Vault-Token", key.token.token).get(Path).map {
        case Success(res) => re.extract[Option[String]](res, "data/value").map { value =>
          Success(SingleSecretValue(value))
        }.getOrElse(Failure(OperationError("Invalid response format.", 503)))
        case Failure(x) => Failure(x)
      }
    }
  }

  object SecretListOperation extends Operation[SecretList, SecretListValue] {
    def op(vault: VaultLocation, key: SecretList, api: Api, re: ResponseExtractor)(implicit ec: ExecutionContext): Future[Try[SecretListValue]] = {
      val Path = "/v1/secret?list=true"
      api.to(vault).header("X-Vault-Token", key.token.token).get(Path).map {
        case Success(res) => Success(SecretListValue(re.extract[Seq[String]](res, "data/value")))
        case Failure(x) => Failure(x)
      }
    }
  }

  implicit def singleSecretOperation: Operation[SingleSecret, SingleSecretValue] = SingleSecretOperation
  implicit def secretListOperation: Operation[SecretList, SecretListValue] = SecretListOperation
}
