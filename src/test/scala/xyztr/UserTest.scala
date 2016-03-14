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

  "User" can "add friends to bubbles" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = BubbleCreator.create("Bubble name", mats, mats.friends.toSet)

    bubble.hasMember(mats.friends.head) should be(true)
  }

  "Invited User" can "decrypt encryption key from bubble invitation, after being added to the bubble" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = BubbleCreator.create("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createSymmetricEncryptionKey
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val invitations = mats.friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptWithPublicKey(bubbleEncryptionKey.getEncoded, f.publicKey)))

    val decryptedBubbleEncryptionKey = Crypto.decryptWithPrivateKey(invitations.head.encryptedEncryptionKey, bengt.privateKey())
    decryptedBubbleEncryptionKey should be(bubbleEncryptionKey.getEncoded)
  }

  "Invited User" can "decrypt bubble from decrypted encryption key from bubble invitation, after being added to the bubble" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = BubbleCreator.create("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createSymmetricEncryptionKey
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val invitations = mats.friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptSymmetricKeyWithPublicKey(bubbleEncryptionKey, f.publicKey)))

    val decryptedBubbleEncryptionKey = Crypto.decryptSymmetricKeyWithPrivateKey(invitations.head.encryptedEncryptionKey, bengt.privateKey())

    val fetchedBubbleFromIpfs = IPFSProxy.receive(ipfsHash, decryptedBubbleEncryptionKey)

    fetchedBubbleFromIpfs should be(bubble.allDataAsBytes())
  }
}
