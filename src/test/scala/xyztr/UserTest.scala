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

  "User" can "add friends to bubbles" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = new Bubble("Bubble name", mats)
    bubble.addFriend(mats.friends.head)

    bubble.hasMember(mats.friends.head) should be(true)
  }

  "Invited User" can "decrypt encryption key from bubble invitation, after being added to the bubble" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = new Bubble("Bubble name", mats)
    bubble.addFriend(mats.friends.head)

    val invitation = bubble.createBubbleInvitations().head
    val decryptedBubbleEncryptionKey = Crypto.decrypt(invitation.encryptedEncryptionKey, bengt.privateKey())
    decryptedBubbleEncryptionKey should be(bubble.encryptionKey.getEncoded)
  }
}
