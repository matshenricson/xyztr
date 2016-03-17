package xyztr

import org.scalatest.{FlatSpec, Matchers}

class BubbleTest extends FlatSpec with Matchers {
  "Bubble" should "should have its creator among the members" in {
    val mats = User("Mats Henricson")
    val bubble = Bubble("Bubble name", mats, Set.empty)
    Crypto.encodedKeysAreEqual(bubble.members.head.encodedPublicKey, mats.publicKey.getEncoded) should be(true)
  }
}
