package xyztr

import org.scalatest.{FlatSpec, Matchers}

class UserTest extends FlatSpec with Matchers {
  "User" should "have a public key" in {
    val mats = User("Mats Henricson", KeyGen.createPrivatePublicPair())
    val publicKey = mats.publicKey()
    publicKey should not be null
  }
}
