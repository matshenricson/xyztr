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
object TierionClient {
  implicit val formats = Serialization.formats(NoTypeHints)

  val tierionDatastoreId = 533
  val tierionUsername = "mats@henricson.se"
  val tierionApiKey = "gJHu+wHGIrqceQu+qUWETqmtB4k9ER5GwyZdC/lQ9vA="

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
    request.headerMap.add("X-Username", tierionUsername)
    request.headerMap.add("X-Api-Key", tierionApiKey)
    request.headerMap.add("Content-Type", "application/json")

    request
  }

  case class BubbleSha256(sha256: String, datastoreId: Int = tierionDatastoreId)
  case class SaveBubbleRecordResponse(id: String, accountId: Int, datastoreId: Int, status: String, json: String, sha256: String, timestamp: Int)  // Ignoring "data"

  def saveBubbleRecord(sha256: String): Future[Option[SaveBubbleRecordResponse]] = {
    val request = createRequest(http.Method.Post, s"/v1/records?datastoreId=$tierionDatastoreId")
    request.setContentString(write(BubbleSha256(sha256)))

    client(request) map { response =>
      response.status match {
        case Status.Ok =>
          Some(read[SaveBubbleRecordResponse](response.getContentString()))
        case _ =>
          None
      }
    }
  }
}
