package xyztr

import org.scalatest.{FlatSpec, Matchers}

class BubbleTest extends FlatSpec with Matchers {
  "Bubble" should "get a hash of all hashes" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bubble = new Bubble("Bubble name", mats)
    val hashOfHashes = bubble.hashOfHashes()
    hashOfHashes should be("DYZauoSgqMci2VmkY58PEptjs75ScguVvfYuy2xHyWLc")
  }
}
