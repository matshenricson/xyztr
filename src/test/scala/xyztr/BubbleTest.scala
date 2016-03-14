package xyztr

import org.ipfs.api.Base58
import org.scalatest.{FlatSpec, Matchers}

class BubbleTest extends FlatSpec with Matchers {
  "Bubble" should "should have its creator among the members" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bubble = BubbleCreator.create("Bubble name", mats, Set.empty)
    bubble.members.head.base58EncodedPublicKey should be(Base58.encode(mats.publicKey().getEncoded))
  }
}
