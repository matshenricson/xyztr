package xyztr

import org.scalatest.{FlatSpec, Matchers}

class BubbleTest extends FlatSpec with Matchers {
  "Bubble" should "get a hash of all bytes" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bubble = new Bubble("Bubble name", mats, Set.empty)
    val hashOfBytes = bubble.hashOfBytes()
    hashOfBytes.length should be(44)
  }
}
