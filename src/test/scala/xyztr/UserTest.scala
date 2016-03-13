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
    val bubble = new Bubble("Bubble name", mats, Set.empty)
  }

  "User" can "add friends to bubbles" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = new Bubble("Bubble name", mats, mats.friends.toSet)

    bubble.hasMember(mats.friends.head) should be(true)
  }

  "Invited User" can "decrypt encryption key from bubble invitation, after being added to the bubble" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = new Bubble("Bubble name", mats, mats.friends.toSet)
    val ipfsHash = IPFS.send(bubble)
    val invitations = mats.friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptWithPublicKey(bubble.encryptionKey.getEncoded, f.publicKey)))

    val decryptedBubbleEncryptionKey = Crypto.decryptWithPrivateKey(invitations.head.encryptedEncryptionKey, bengt.privateKey())
    decryptedBubbleEncryptionKey should be(bubble.encryptionKey.getEncoded)
  }

  "Invited User" can "decrypt bubble from decrypted encryption key from bubble invitation, after being added to the bubble" in {
    val mats = new User("Mats Henricson", Crypto.createPrivatePublicPair())
    val bengt = new User("Bengt Henricson", Crypto.createPrivatePublicPair())

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = new Bubble("Bubble name", mats, mats.friends.toSet)
    val ipfsHash = IPFS.send(bubble)
    val invitations = mats.friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptSymmetricKeyWithPublicKey(bubble.encryptionKey, f.publicKey)))

    val decryptedBubbleEncryptionKey = Crypto.decryptSymmetricKeyWithPrivateKey(invitations.head.encryptedEncryptionKey, bengt.privateKey())

    val fetchedBubbleFromIpfs = IPFS.receive(ipfsHash, decryptedBubbleEncryptionKey).getOrElse(throw new AssertionError("Weird"))
    val fetchedBubbleHash = Hasher.base58HashFromBytes(fetchedBubbleFromIpfs)

    fetchedBubbleHash should be(bubble.hashOfBytes())
  }
}
