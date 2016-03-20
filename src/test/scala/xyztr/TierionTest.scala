package xyztr

import com.twitter.util.Await
import org.scalatest.{FlatSpec, Matchers}

class TierionTest extends FlatSpec with Matchers {
  "TierionClient" can "create a bubble record" in {
    val future = TierionClient.saveBubbleRecord("ipfsHash")
    Await.result(future.onSuccess {
      option => if (option.isDefined) {
        option.get.id.length should be > 10
      }
    })
  }
}
