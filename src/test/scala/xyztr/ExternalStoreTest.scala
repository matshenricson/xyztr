package xyztr

import java.math.BigInteger
import java.util.Date

import org.scalatest.{FlatSpec, Matchers}

class ExternalStoreTest extends FlatSpec with Matchers {
  val blockchainHashId = "blockchainHashId"
  val ipfsHash = "ipfsHash"

  "CoreUserData" can "be created in a natural way" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    mats.addBubble(BubbleHandle(ipfsHash, bubbleEncryptionKey, mats.publicKey, None))

    val coreUserData = CoreUserData(mats)
    Crypto.privateKeysAreEqual(
      Crypto.getPrivateKeyFromBigIntegers(coreUserData.privateKeyBigIntegerComponentsAsStrings.map(s => new BigInteger(s))),
      mats.privateKey) should be(true)
    Crypto.encodedKeysAreEqual(coreUserData.encodedPublicKey, mats.publicKey.getEncoded) should be(true)
    coreUserData.name should be(mats.name)
    coreUserData.friends.size should be(1)
    Crypto.encodedKeysAreEqual(coreUserData.friends.head.encodedPublicKeyOfFriend, bengt.publicKey.getEncoded) should be(true)
    coreUserData.bubbles.size should be(mats.bubblesMap.values.size)
    coreUserData.bubbles.head.blockchainHashId should be(None)
  }

  "CoreUserData" can "be serialized and deserialized to JSON" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    mats.addBubble(BubbleHandle(ipfsHash, Crypto.encryptWithPublicKey(bubbleEncryptionKey.getEncoded, mats.publicKey), new Date().getTime, Some(blockchainHashId)))
    val coreUserData = CoreUserData(mats)

    val json = JSON.toJsonString(coreUserData)
    val newCoreUserData = JSON.fromJsonString[CoreUserData](json)

    coreUserData.privateKeyBigIntegerComponentsAsStrings should be(newCoreUserData.privateKeyBigIntegerComponentsAsStrings)
    coreUserData.encodedPublicKey should be(newCoreUserData.encodedPublicKey)
    coreUserData.name should be(newCoreUserData.name)
    coreUserData.friends.size should be(newCoreUserData.friends.size)
    coreUserData.friends.head.encodedPublicKeyOfFriend should be(newCoreUserData.friends.head.encodedPublicKeyOfFriend)
    coreUserData.friends.head.friendName should be(newCoreUserData.friends.head.friendName)
    coreUserData.bubbles.size should be(newCoreUserData.bubbles.size)
    coreUserData.bubbles.head.blockchainHashId.get should be(blockchainHashId)
  }

  val password = "PASSWORD"

  "User" can "be saved to file" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    mats.addBubble(BubbleHandle(ipfsHash, Crypto.encryptWithPublicKey(Crypto.createNewSymmetricEncryptionKey().getEncoded, mats.publicKey),
      new Date().getTime, Some(blockchainHashId)))

    ExternalStore.save(mats, password)
  }

  "User" can "be retrieved from file" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt)
    mats.acceptFriendRequest(fr)

    mats.addBubble(BubbleHandle(ipfsHash, Crypto.encryptWithPublicKey(Crypto.createNewSymmetricEncryptionKey().getEncoded, mats.publicKey),
      new Date().getTime, Some(blockchainHashId)))

    ExternalStore.save(mats, password)

    val newUser = ExternalStore.retrieve(password)

    newUser.name should be(mats.name)
    Crypto.publicKeysAreEqual(newUser.publicKey, mats.publicKey) should be(true)
    Crypto.privateKeysAreEqual(newUser.privateKey, mats.privateKey) should be(true)
    newUser.friends.size should be(mats.friends.size)
    newUser.friends.size should be(1)
    Crypto.encodedKeysAreEqual(newUser.friends.head.encodedPublicKeyOfFriend, mats.friends.head.encodedPublicKeyOfFriend) should be(true)
    newUser.friends.head.friendName should be(mats.friends.head.friendName)
    newUser.bubblesMap.values.size should be(mats.bubblesMap.values.size)
    newUser.bubblesMap.values.size should be(1)
    newUser.bubblesMap.values.head.ipfsHash should be(mats.bubblesMap.values.head.ipfsHash)
    newUser.bubblesMap.values.head.blockchainHashId.get should be(blockchainHashId)
    Crypto.encodedKeysAreEqual(newUser.bubblesMap.values.head.encodedEncryptedEncryptionKey, mats.bubblesMap.values.head.encodedEncryptedEncryptionKey) should be(true)
  }

  "User" can "be recreated from a password" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")
    val symmetricKey = Crypto.createNewSymmetricEncryptionKey()

    // Create in different scope, so we aren't tempted to use after the load from disk
    {
      val fr = FriendRequest(bengt)
      mats.acceptFriendRequest(fr)

      mats.addBubble(BubbleHandle(ipfsHash, Crypto.encryptWithPublicKey(symmetricKey.getEncoded, mats.publicKey), new Date().getTime, Some(blockchainHashId)))

      ExternalStore.save(mats, password)
    }

    // OK, now lets try to load the user from disk
    val recreatedMats = ExternalStore.retrieve(password)

    // TODO: Can we use "recreatedMats shouldEqual mats" instead ????
    recreatedMats.name should be(mats.name)
    Crypto.privateKeysAreEqual(recreatedMats.privateKey, mats.privateKey) should be(true)
    Crypto.publicKeysAreEqual(recreatedMats.publicKey, mats.publicKey) should be(true)
    recreatedMats.bubblesMap.values.size should be(1)
    recreatedMats.bubblesMap.values.head.ipfsHash should be(ipfsHash)
    recreatedMats.bubblesMap.values.head.blockchainHashId.get should be(blockchainHashId)
    Crypto.encodedKeysAreEqual(recreatedMats.bubblesMap.values.head.encodedEncryptedEncryptionKey, mats.bubblesMap.values.head.encodedEncryptedEncryptionKey) should be(true)
    recreatedMats.friends.size should be(1)
    Crypto.encodedKeysAreEqual(recreatedMats.friends.head.encodedPublicKeyOfFriend, bengt.publicKey.getEncoded) should be(true)
    recreatedMats.friends.head.friendName should be(bengt.name)
  }
}
