package xyztr

import org.scalatest.{FlatSpec, Matchers}

class UserTest extends FlatSpec with Matchers {
  "User" should "have a public key" in {
    val mats = new User("Mats Henricson", KeyGen.createPrivatePublicPair())
    val publicKey = mats.publicKey()
    publicKey should not be null
  }

  "User" should "get friends" in {
    val mats = new User("Mats Henricson", KeyGen.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", KeyGen.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    mats.hasFriend(bengt.publicKey()) should be(true)
  }
}
