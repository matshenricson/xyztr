package xyztr

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http
import com.twitter.finagle.http._
import com.twitter.util.Future
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

/**
  * Uses the Tierion REST API to save hashes of bubbles.
  */
case class SubscriptionRequest(productIds: Long)

case class SubscriptionResponse(id: String)

object TierionClient {
  implicit val formats = Serialization.formats(NoTypeHints)

  private val client = ClientBuilder()
      .name("tierion")
      .hosts("api.tierion.com:443")
      .tls("api.tierion.com")
      .codec(com.twitter.finagle.http.Http())
      .hostConnectionLimit(1).build()

  def createRequest(method: Method, uri: String) = {
    val request = http.Request(method, "/")
    request.uri = uri
    request.host = "api.tierion.com"
    request.headerMap.add("X-Username", "mats@henricson.se")
    request.headerMap.add("X-Api-Key", "gJHu+wHGIrqceQu+qUWETqmtB4k9ER5GwyZdC/lQ9vA=")
    request.headerMap.add("Content-Type", "application/json")

    request
  }

  def getSubscription: Future[Option[SubscriptionResponse]] = {
    val request = createRequest(Method.Get, "/v1/datastores/533")

    client(request) map { response => response.status match {
      case Status.Ok =>
        println("Datastore data: " + response.encodeString())
        Some(read[SubscriptionResponse]("{\"id\":\"" + response.getStatusCode() + "\"}"))
      case _ =>
        println("Something went wrong: " + response.toString())
        None
      }
    }
  }

  def createRecord(productId: Long): Future[Unit] = {
    val request = createRequest(http.Method.Put, "v1/records")
    request.setContentString(write(SubscriptionRequest(productId)))

    client(request) map { response =>
      response.status match {
        case Status.Ok | Status.NoContent => ()
        case _ => throw new UnsupportedOperationException("xxxxxx")
      }
    }
  }
}
