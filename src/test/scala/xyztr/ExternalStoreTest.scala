package xyztr

import java.math.BigInteger

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
    mats.bubbles.add(BubbleHandle(ipfsHash, bubbleEncryptionKey, mats.publicKey))

    val coreUserData = CoreUserData(mats)
    Crypto.privateKeysAreEqual(
      Crypto.getPrivateKeyFromBigIntegers(coreUserData.privateKeyBigIntegerComponentsAsStrings.map(s => new BigInteger(s))),
      mats.privateKey) should be(true)
    Crypto.encodedKeysAreEqual(coreUserData.encodedPublicKey, mats.publicKey.getEncoded) should be(true)
    coreUserData.name should be(mats.name)
    coreUserData.friends.size should be(1)
    Crypto.encodedKeysAreEqual(coreUserData.friends.head.encodedPublicKeyOfFriend, bengt.publicKey.getEncoded) should be(true)
    coreUserData.bubbles.size should be(mats.bubbles.size)
  }

  "CoreUserData" can "be serialized and deserialized to JSON" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    mats.bubbles.add(BubbleHandle("ipfsHash", bubbleEncryptionKey, mats.publicKey))
    val coreUserData = CoreUserData(mats)

    val json = write(coreUserData)
    val newCoreUserData = read[CoreUserData](json)

    coreUserData.privateKeyBigIntegerComponentsAsStrings should be(newCoreUserData.privateKeyBigIntegerComponentsAsStrings)
    coreUserData.encodedPublicKey should be(newCoreUserData.encodedPublicKey)
    coreUserData.name should be(newCoreUserData.name)
    coreUserData.friends.size should be(newCoreUserData.friends.size)
    coreUserData.friends.head.encodedPublicKeyOfFriend should be(newCoreUserData.friends.head.encodedPublicKeyOfFriend)
    coreUserData.friends.head.friendName should be(newCoreUserData.friends.head.friendName)
    coreUserData.bubbles.size should be(newCoreUserData.bubbles.size)
  }

  val password = "PASSWORD"

  "CoreUserData" can "be saved to file" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    mats.bubbles.add(BubbleHandle("fakeIpfsHash", Crypto.createNewSymmetricEncryptionKey(), mats.publicKey))
    val coreUserData = CoreUserData(mats)

    val secretKeyFromPassword = Crypto.reCreateSecretKey(password)

    ExternalStore.save(coreUserData, secretKeyFromPassword)
  }

  "CoreUserData" can "be retrieved from file" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    mats.bubbles.add(BubbleHandle("fakeIpfsHash", Crypto.createNewSymmetricEncryptionKey(), mats.publicKey))
    val coreUserData = CoreUserData(mats)

    val secretKeyFromPassword = Crypto.reCreateSecretKey(password)

    ExternalStore.save(coreUserData, secretKeyFromPassword)

    val newCoreUserData = ExternalStore.retrieve(secretKeyFromPassword)

    newCoreUserData.name should be(coreUserData.name)
    newCoreUserData.encodedPublicKey should be(coreUserData.encodedPublicKey)
    newCoreUserData.privateKeyBigIntegerComponentsAsStrings should be(coreUserData.privateKeyBigIntegerComponentsAsStrings)
    newCoreUserData.friends.size should be(coreUserData.friends.size)
    newCoreUserData.friends.size should be(1)
    newCoreUserData.friends.head.encodedPublicKeyOfFriend should be(coreUserData.friends.head.encodedPublicKeyOfFriend)
    newCoreUserData.friends.head.friendName should be(coreUserData.friends.head.friendName)
    newCoreUserData.bubbles.size should be(coreUserData.bubbles.size)
    newCoreUserData.bubbles.size should be(1)
    newCoreUserData.bubbles.head.ipfsHash should be(coreUserData.bubbles.head.ipfsHash)
    newCoreUserData.bubbles.head.encodedEncryptedEncryptionKey.get should be(coreUserData.bubbles.head.encodedEncryptedEncryptionKey.get)
    newCoreUserData.bubbles.head.isBubbleEncrypted should be(coreUserData.bubbles.head.isBubbleEncrypted)
  }

  "User" can "be recreated from a password" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")
    val ipfsHash = "fakeIpfsHash"
    val symmetricKey = Crypto.createNewSymmetricEncryptionKey()

    // Create in different scope, so we aren't tempted to use after the load from disk
    {
      val fr = FriendRequest(bengt)
      mats.acceptFriendRequest(fr)

      mats.bubbles.add(BubbleHandle(ipfsHash, symmetricKey, mats.publicKey))
      val matsUserData = CoreUserData(mats)

      val secretKeyFromPassword = Crypto.reCreateSecretKey(password)

      ExternalStore.save(matsUserData, secretKeyFromPassword)
    }

    // OK, now lets try to load the user from disk
    val recreatedMats = User.fromPassword(password)

    // TODO: Can we use "recreatedMats shouldEqual mats" instead ????
    recreatedMats.name should be(mats.name)
    Crypto.privateKeysAreEqual(recreatedMats.privateKey, mats.privateKey) should be(true)
    Crypto.publicKeysAreEqual(recreatedMats.publicKey, mats.publicKey) should be(true)
    recreatedMats.bubbles.size should be(1)
    recreatedMats.bubbles.head.ipfsHash should be(ipfsHash)
    Crypto.encodedKeysAreEqual(recreatedMats.bubbles.head.encodedEncryptedEncryptionKey.get, mats.bubbles.head.encodedEncryptedEncryptionKey.get) should be(true)
    recreatedMats.friends.size should be(1)
    Crypto.encodedKeysAreEqual(recreatedMats.friends.head.encodedPublicKeyOfFriend, bengt.publicKey.getEncoded) should be(true)
    recreatedMats.friends.head.friendName should be(bengt.name)
  }
}
