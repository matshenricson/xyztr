package xyztr

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.scalatest.{FlatSpec, Matchers}

class ExternalStoreTest extends FlatSpec with Matchers {
  implicit val formats = Serialization.formats(NoTypeHints)

  "CoreUserData" can "be created in a natural way" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val invitations = mats.friends.map(f => BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey))

    val coreUserData = CoreUserData(mats, Set(BubbleHandle(ipfsHash, bubbleEncryptionKey, mats.publicKey())))
    coreUserData.encodedPrivateKey.toSeq should be(mats.privateKey().getEncoded.toSeq)
    coreUserData.encodedPublicKey.toSeq should be(mats.publicKey().getEncoded.toSeq)
    coreUserData.name should be(mats.name)
    coreUserData.friends.size should be(1)
    coreUserData.friends.head.encodedPublicKey.toSeq should be(bengt.publicKey().getEncoded)
    coreUserData.bubbles.size should be(1)
  }

  "CoreUserData" can "be serialized and deserialized to JSON" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val coreUserData = CoreUserData(mats, Set(BubbleHandle("ipfsHash", bubbleEncryptionKey, mats.publicKey())))

    val json = write(coreUserData)
    val newCoreUserData = read[CoreUserData](json)

    coreUserData.encodedPrivateKey should be(newCoreUserData.encodedPrivateKey)
    coreUserData.encodedPublicKey should be(newCoreUserData.encodedPublicKey)
    coreUserData.name should be(newCoreUserData.name)
    coreUserData.friends.size should be(newCoreUserData.friends.size)
    coreUserData.friends.head.encodedPublicKey should be(newCoreUserData.friends.head.encodedPublicKey)
    coreUserData.friends.head.name should be(newCoreUserData.friends.head.name)
    coreUserData.bubbles.size should be(newCoreUserData.bubbles.size)
  }

  val password = "PASSWORD"

  "CoreUserData" can "be saved to file" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val fakeBubbleHandle = BubbleHandle("fakeIpfsHash", Crypto.createNewSymmetricEncryptionKey(), mats.publicKey())
    val coreUserData = CoreUserData(mats, Set(fakeBubbleHandle))

    val secretKeyFromPassword = Crypto.reCreateSecretKey(password)

    ExternalStore.save(coreUserData, secretKeyFromPassword)
  }

  "CoreUserData" can "be retrieved from file" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val fakeBubbleHandle = BubbleHandle("fakeIpfsHash", Crypto.createNewSymmetricEncryptionKey(), mats.publicKey())
    val coreUserData = CoreUserData(mats, Set(fakeBubbleHandle))

    val secretKeyFromPassword = Crypto.reCreateSecretKey(password)

    ExternalStore.save(coreUserData, secretKeyFromPassword)

    val newCoreUserData = ExternalStore.retrieve(secretKeyFromPassword)

    newCoreUserData.name should be(coreUserData.name)
    newCoreUserData.encodedPublicKey should be(coreUserData.encodedPublicKey)
    newCoreUserData.encodedPrivateKey should be(coreUserData.encodedPrivateKey)
    newCoreUserData.friends.size should be(coreUserData.friends.size)
    newCoreUserData.friends.size should be(1)
    newCoreUserData.friends.head.encodedPublicKey should be(coreUserData.friends.head.encodedPublicKey)
    newCoreUserData.friends.head.name should be(coreUserData.friends.head.name)
    newCoreUserData.bubbles.size should be(coreUserData.bubbles.size)
    newCoreUserData.bubbles.size should be(1)
    newCoreUserData.bubbles.head.ipfsHash should be(coreUserData.bubbles.head.ipfsHash)
    newCoreUserData.bubbles.head.encodedEncryptedEncryptionKey.get should be(coreUserData.bubbles.head.encodedEncryptedEncryptionKey.get)
    newCoreUserData.bubbles.head.isBubbleEncrypted should be(coreUserData.bubbles.head.isBubbleEncrypted)
  }
}
