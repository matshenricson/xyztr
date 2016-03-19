package xyztr

import com.twitter.util.Await
import org.scalatest.{FlatSpec, Matchers}

class TierionTest extends FlatSpec with Matchers {
  "TierionClient" can "create a bubble record" in {
    val mats = User("Mats Henricson")
    val bubble = Bubble("Bubble name", mats, Set.empty)
    val future = TierionClient.saveBubbleRecord(bubble.sha256AsBase64)
    Await.result(future.onSuccess {
      option => if (option.isDefined) {
        option.get.id.length should be > 10
      }
    })
  }
}
