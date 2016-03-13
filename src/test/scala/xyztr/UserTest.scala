package xyztr

import org.scalatest.{FlatSpec, Matchers}

class UserTest extends FlatSpec with Matchers {
  "User" should "have a public key" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val publicKey = mats.publicKey()
    publicKey should not be null
  }

  "User" can "get friends" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    mats.hasFriend(bengt.publicKey()) should be(true)
  }

  "User" can "create bubbles" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bubble = new Bubble("Bubble name", mats)
  }
}
