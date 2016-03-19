package xyztr

import org.scalatest.{FlatSpec, Matchers}

class UserTest extends FlatSpec with Matchers {
  "User" should "have a public key" in {
    val mats = User("Mats Henricson")
    val publicKey = mats.publicKey
    publicKey should not be null
  }

  "User" can "get friends" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    mats.hasFriend(bengt.publicKey.getEncoded) should be(true)
  }

  "User" can "add friends to bubbles" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    bubble.hasMember(mats.friends.head) should be(true)
  }

  "Invited User" can "decrypt encryption key from bubble handle, after being added to the bubble" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val handles = mats.friends.map(f => BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey))

    val decryptedBubbleEncryptionKey = handles.head.decryptSecretKey(bengt.privateKey)
    decryptedBubbleEncryptionKey.getEncoded should be(bubbleEncryptionKey.getEncoded)
  }

  "Invited User" can "decrypt bubble from decrypted encryption key from bubble handle, after being added to the bubble" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val handles = mats.friends.map(f => BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey))

    val decryptedBubbleEncryptionKey = handles.head.decryptSecretKey(bengt.privateKey)

    val fetchedBubbleFromIpfs = IPFSProxy.receive(ipfsHash, decryptedBubbleEncryptionKey)

    fetchedBubbleFromIpfs.name should be(bubble.name)
    fetchedBubbleFromIpfs.bubbleType should be(bubble.bubbleType)
    fetchedBubbleFromIpfs.creatorName should be(bubble.creatorName)
    fetchedBubbleFromIpfs.startTime should be(bubble.startTime)
    fetchedBubbleFromIpfs.stopTime should be(bubble.stopTime)
    fetchedBubbleFromIpfs.members.size should be(bubble.members.size)
    fetchedBubbleFromIpfs.members.head.name should be(bubble.members.head.name)
    fetchedBubbleFromIpfs.members.head.encodedPublicKey should be(bubble.members.head.encodedPublicKey)
    fetchedBubbleFromIpfs.members.tail.head.name should be(bubble.members.tail.head.name)
    fetchedBubbleFromIpfs.members.tail.head.encodedPublicKey should be(bubble.members.tail.head.encodedPublicKey)
  }
}
