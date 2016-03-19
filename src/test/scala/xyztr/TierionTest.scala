package xyztr

import com.twitter.util.Await
import org.scalatest.{FlatSpec, Matchers}

class TierionTest extends FlatSpec with Matchers {
  "TierionClient" can "talk to Tierion" in {
    val future = TierionClient.getSubscription
    Await.result(future.onSuccess {
      option => if (option.isDefined) {
        println("Status code: " + option.get.id)
        option.get.id should be("200")
      }
    })
  }
}
