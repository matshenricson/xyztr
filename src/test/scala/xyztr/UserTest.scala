package xyztr

import com.twitter.util.Await
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
    val response = Await.result(TierionClient.saveBubbleRecord(ipfsHash))     // TODO: Timeout?
    val handles = mats.friends.map(f => BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey, response))

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
    val response = Await.result(TierionClient.saveBubbleRecord(ipfsHash))   // TODO: Timeout ???
    val handles = mats.friends.map(f => BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey, response))

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

  "MultiMap" can "be understood..." in {
    val mats = User("Mats")

    val b1 = Bubble("Bubble 1", mats, mats.friends.toSet)
    val b1Key = Crypto.createNewSymmetricEncryptionKey()

    val b2 = Bubble("Bubble 2", mats, mats.friends.toSet)
    val b2Key = Crypto.createNewSymmetricEncryptionKey()

    val b1Handle = BubbleHandle("ipfsHash1", b1Key, mats.publicKey)
    val b2Handle = BubbleHandle("ipfsHash2", b2Key, mats.publicKey)
    mats.addBubbleHandle(b1Handle)
    mats.addBubbleHandle(b2Handle)

    mats.getAllBubbleHandles.size should be(2)

    // Now comes the kicker, create a new Bubble Handle, which is a new version of b1Handle
    val b1PrimeHandle = BubbleHandle("ipfsHash1Prime", b1Key, mats.publicKey)
    mats.addBubbleHandle(b1PrimeHandle)

    mats.getAllBubbleHandles.size should be(3)
    CoreUserData(mats).bubbleHandles.size should be(3)

    // If I now ask for the latest, using the b1Handle, I should get b1PrimeHandle, since it was created later
    val latestBubbleHandle = mats.getLatestBubbleHandle(b1Handle)
    latestBubbleHandle.ipfsHash should be(b1PrimeHandle.ipfsHash)
    latestBubbleHandle.created should be(b1PrimeHandle.created)
  }
}
