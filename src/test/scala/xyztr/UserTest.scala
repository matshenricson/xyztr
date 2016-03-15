package xyztr

import java.util.Date

import org.scalatest.{FlatSpec, Matchers}

class UserTest extends FlatSpec with Matchers {
  "User" should "have a public key" in {
    val mats = User("Mats Henricson")
    val publicKey = mats.publicKey()
    publicKey should not be null
  }

  "User" can "get friends" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    mats.hasFriend(bengt.publicKey()) should be(true)
  }

  "User" can "add friends to bubbles" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    bubble.hasMember(mats.friends.head) should be(true)
  }

  "Invited User" can "decrypt encryption key from bubble invitation, after being added to the bubble" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val invitations = mats.friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptWithPublicKey(bubbleEncryptionKey.getEncoded, f.publicKey)))

    val decryptedBubbleEncryptionKey = Crypto.decryptWithPrivateKey(invitations.head.encryptedEncryptionKey, bengt.privateKey())
    decryptedBubbleEncryptionKey should be(bubbleEncryptionKey.getEncoded)
  }

  "Invited User" can "decrypt bubble from decrypted encryption key from bubble invitation, after being added to the bubble" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val invitations = mats.friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptSymmetricKeyWithPublicKey(bubbleEncryptionKey, f.publicKey)))

    val decryptedBubbleEncryptionKey = Crypto.decryptSymmetricKeyWithPrivateKey(invitations.head.encryptedEncryptionKey, bengt.privateKey())

    val fetchedBubbleFromIpfs = IPFSProxy.receive(ipfsHash, decryptedBubbleEncryptionKey)

    fetchedBubbleFromIpfs should be(bubble)
  }

  "User" can "inspect plain text bubble if its encryption is turned off" in {
    val mats = User("Mats Henricson")
    val bubble = Bubble("Bubble name", mats, new Date().getTime, 0, Set.empty[Friend], "Landskamp", encrypted = false)
    val ipfsHash = IPFSProxy.send(bubble)                        // No key needed to send to IPFS
    val fetchedBubbleFromIpfs = IPFSProxy.receive(ipfsHash)      // No key needed to receive from IPFS

    fetchedBubbleFromIpfs should be(bubble)
  }
}
