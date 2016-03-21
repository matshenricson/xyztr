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

  "BubbleHandle" can "be sorted" in {
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

  "BubbleHandle" can "be chronologically fetched from user" in {
    val mats = User("Mats")

    val b1Key = Crypto.createNewSymmetricEncryptionKey()
    val b2Key = Crypto.createNewSymmetricEncryptionKey()

    val b1Handle = BubbleHandle("ipfsHash1", b1Key, mats.publicKey)
    val b2Handle = BubbleHandle("ipfsHash2", b2Key, mats.publicKey)
    mats.addBubbleHandle(b1Handle)
    mats.addBubbleHandle(b2Handle)

    // Create a new Bubble Handle, which is a new version of b1Handle
    val b1PrimeHandle = BubbleHandle("ipfsHash1Prime", b1Key, mats.publicKey)
    mats.addBubbleHandle(b1PrimeHandle)

    mats.getAllBubbleHandles.size should be(3)

    val bubbleList = mats.bubblesInChronologicalOrder

    bubbleList.size should be(2)
    bubbleList.head.ipfsHash should be("ipfsHash1Prime")   // Newest bubble
    bubbleList.tail.head.ipfsHash should be("ipfsHash2")   // Oldest bubble
  }
}
