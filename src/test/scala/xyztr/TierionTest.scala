package xyztr

import com.twitter.util.Await
import org.scalatest.{FlatSpec, Matchers}

class TierionTest extends FlatSpec with Matchers {
  "TierionClient" can "talk to Tierion" in {
    val optionOfSubscriptionResponseFuture = TierionClient.getSubscription
    Await.result(optionOfSubscriptionResponseFuture.onSuccess {optionSubscription => if (optionSubscription.isDefined) println("GET success: " + optionSubscription.get.id)})
  }
}
