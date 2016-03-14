package xyztr

import org.scalatest.{FlatSpec, Matchers}

class BubbleTest extends FlatSpec with Matchers {
  "Bubble" should "get a hash of all bytes" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bubble = new Bubble("Bubble name", mats, Set.empty)
    val sha256 = bubble.sha256OfData()
    sha256.length should be <= 44
  }

  "Bubble" should "should have its creator among the members" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bubble = new Bubble("Bubble name", mats, Set.empty)
    bubble.members.head.publicKey should be(mats.publicKey())
  }
}
