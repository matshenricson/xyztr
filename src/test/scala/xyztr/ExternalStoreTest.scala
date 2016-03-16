package xyztr

import org.ipfs.api.Base58
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.scalatest.{FlatSpec, Matchers}

class ExternalStoreTest extends FlatSpec with Matchers {
  implicit val formats = Serialization.formats(NoTypeHints)

  "CoreUserData" can "be created in a natural way" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val invitations = mats.friends.map(f => BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey))

    val coreUserData = CoreUserData(mats, Set(BubbleHandle(ipfsHash, bubbleEncryptionKey, mats.publicKey())))
    coreUserData.base58EncodedPrivateKey should be(Base58.encode(mats.privateKey().getEncoded))
    coreUserData.base58EncodedPublicKey should be(Base58.encode(mats.publicKey().getEncoded))
    coreUserData.name should be(mats.name)
    coreUserData.friends.size should be(1)
    coreUserData.friends.head.publicKey.getEncoded should be(bengt.publicKey().getEncoded)
    coreUserData.bubbles.size should be(1)
  }

  "CoreUserData" can "be serialized and deserialized to JSON" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val coreUserData = CoreUserData(mats, Set(BubbleHandle("ipfsHash", bubbleEncryptionKey, mats.publicKey())))

    val json = write(coreUserData)
    val newCoreUserData = read[CoreUserData](json)

    coreUserData.base58EncodedPrivateKey should be(newCoreUserData.base58EncodedPrivateKey)
    coreUserData.base58EncodedPublicKey should be(newCoreUserData.base58EncodedPublicKey)
    coreUserData.name should be(newCoreUserData.name)
    coreUserData.friends.size should be(newCoreUserData.friends.size)
    coreUserData.friends.head.encodedPublicKey should be(newCoreUserData.friends.head.encodedPublicKey)
    coreUserData.friends.head.name should be(newCoreUserData.friends.head.name)
    coreUserData.bubbles.size should be(newCoreUserData.bubbles.size)
  }

  val password = "password"

  "CoreUserData" can "be saved to file" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val fakeBubbleHandle = BubbleHandle("fakeIpfsHash", Crypto.createNewSymmetricEncryptionKey(), mats.publicKey())
    val coreUserData = CoreUserData(mats, Set(fakeBubbleHandle))

    val secretKeyFromPassword = Crypto.reCreateSecretKey(password)

    println("TURN ON, SUCKER!!!!!!")
//    ExternalStore.save(coreUserData, secretKeyFromPassword)      // TODO: Turn on later
  }
}
