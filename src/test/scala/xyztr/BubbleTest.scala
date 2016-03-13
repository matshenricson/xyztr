package xyztr

import org.scalatest.{FlatSpec, Matchers}

class BubbleTest extends FlatSpec with Matchers {
  "Bubble" should "get a hash of all hashes" in {
    val bubble = Bubble("Bubble name")
    val hashOfHashes = bubble.hashOfHashes()
    hashOfHashes should be("DYZauoSgqMci2VmkY58PEptjs75ScguVvfYuy2xHyWLc")
  }
}
