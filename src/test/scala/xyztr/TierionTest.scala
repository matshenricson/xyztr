package xyztr

import org.scalatest.{FlatSpec, Matchers}

class TierionTest extends FlatSpec with Matchers {
  "TierionClient" can "talk to Tierion" in {
    TierionClient.getSubscription map { option =>
      for {
        subscription <- option
      } yield subscription.id should be(200)
    }
  }
}
