package xyztr

import com.twitter.finagle.http._
import com.twitter.finagle.{Http, Service, http}
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
  private val client: Service[Request, Response] = Http.newService("www.dn.se:80")

  def getSubscription: Future[Option[SubscriptionResponse]] = {
    val request = http.Request(http.Method.Get, "/")
    request.host = "www.dn.se"

    client(request) map { response => response.status match {
      case Status.Ok => Some(read[SubscriptionResponse]("{\"id\":\"" + response.getStatusCode() + "\"}"))
        case _ => None
      }
    }
  }

  def updateSubscription(userId: Long, productId: Long): Future[Unit] = {
    val json = write(SubscriptionRequest(productId))
    val request = http.Request(http.Method.Put, s"/superscription/superscription/v1/users/$userId/subscription")
    request.setContentString(json)

    client(request) map { response =>
      response.status match {
        case Status.Ok | Status.NoContent => ()
        case _ => throw new UnsupportedOperationException("xxxxxx")
      }
    }
  }
}