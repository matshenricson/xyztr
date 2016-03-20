package xyztr

import java.util.Base64

import com.twitter.util.Await
import org.scalatest.{FlatSpec, Matchers}

class ScenarioTest extends FlatSpec with Matchers {
  val matsPassword = "matspassword"
  val bengtPassword = "bengtspassword"
  val matsName = "Mats Henricson"
  val bengtName = "Bengt Henricson"
  val bubbleName = "Bubble name"

  "Abstractions" can "easily be used as intended" in {
    preCreateUsers()
    matsSendsFriendRequestToBengt()
    bengtHandlesFriendRequest_SendsBackFriendResponse()
    matsHandlesFriendResponseFromBengt()
    matsCreatesBubble_SendsItToIpfs_SendsBubbleHandleToBengt()
    bengtGetsBubbleHandle()
    checkThatMatsAndBengtAreMutualFriends()
    checkThatBothHaveTheSameBubbleHandles()
    checkThatBothCanDecryptToTheSameBubble()
    bengtGetsBubble_ChangesIt_SavesToIpfs_SendsBubbleHandleToMats()
    matsGetsBubbleHandle()
    matsGetsBubble_CanSeeThatItIsChanged()
  }

  def checkThatMatsAndBengtAreMutualFriends() = {
    val mats = ExternalStore.retrieve(matsPassword)
    val bengt = ExternalStore.retrieve(bengtPassword)

    mats.friends.size should be(1)
    bengt.friends.size should be(1)
    Crypto.publicKeysAreEqual(mats.friends.head.publicKey, bengt.publicKey)
    Crypto.publicKeysAreEqual(bengt.friends.head.publicKey, mats.publicKey)
    mats.friends.head.friendName should be(bengtName)
    bengt.friends.head.friendName should be(matsName)
  }

  def checkThatBothHaveTheSameBubbleHandles() = {
    val mats = ExternalStore.retrieve(matsPassword)
    val bengt = ExternalStore.retrieve(bengtPassword)

    mats.getAllBubbleHandles.size should be(1)
    bengt.getAllBubbleHandles.size should be(1)
    mats.getAllBubbleHandles.head.ipfsHash should be(bengt.getAllBubbleHandles.head.ipfsHash)
    Crypto.secretKeysAreEqual(mats.getAllBubbleHandles.head.decryptSecretKey(mats.privateKey), bengt.getAllBubbleHandles.head.decryptSecretKey(bengt.privateKey))
  }

  def checkThatBothCanDecryptToTheSameBubble() = {
    val matsBubble = getDecryptedBubbleFromIpfs(matsPassword)
    val bengtBubble = getDecryptedBubbleFromIpfs(bengtPassword)

    matsBubble.creatorName should be(matsName)
    bengtBubble.creatorName should be(matsName)

    matsBubble.bubbleType should be(bengtBubble.bubbleType)
    matsBubble.members.size should be(2)
    matsBubble.members.size should be(bengtBubble.members.size)
    matsBubble.name should be (bengtBubble.name)
    matsBubble.startTime should be(bengtBubble.startTime)
    matsBubble.stopTime should be(bengtBubble.stopTime)
  }

  def getDecryptedBubbleFromIpfs(password: String) = {
    val user = ExternalStore.retrieve(password)
    val bubbleEncryptionKey = user.getAllBubbleHandles.head.decryptSecretKey(user.privateKey)
    IPFSProxy.receive(user.getAllBubbleHandles.head.ipfsHash, bubbleEncryptionKey)
  }

  def preCreateUsers() = {
    val mats = User(matsName)
    val bengt = User(bengtName)
    ExternalStore.save(mats, matsPassword)
    ExternalStore.save(bengt, bengtPassword)
  }

  def matsSendsFriendRequestToBengt() = {
    val mats = ExternalStore.retrieve(matsPassword)

    val friendRequest = FriendRequest(mats)
    UserToUserChannel.sendFriendRequest(bengtName, friendRequest)

    ExternalStore.save(mats, matsPassword)
  }

  def matsHandlesFriendResponseFromBengt() = {
    val mats = ExternalStore.retrieve(matsPassword)

    val friendResponses = UserToUserChannel.getFriendResponse(mats.publicKey.getEncoded)
    friendResponses.map(fr => mats.handleFriendResponse(fr))

    ExternalStore.save(mats, matsPassword)
  }

  def bengtHandlesFriendRequest_SendsBackFriendResponse() = {
    val bengt = ExternalStore.retrieve(bengtPassword)

    // TODO: Rewrite below so that it can handle a collection of FriendRequest objects
    val friendRequest = UserToUserChannel.getFriendRequest(bengtName).get
    val friendResponse = bengt.acceptFriendRequest(friendRequest)
    UserToUserChannel.sendFriendResponse(friendRequest.encodedPublicKeyOfSender, friendResponse)

    ExternalStore.save(bengt, bengtPassword)
  }

  def bengtGetsBubbleHandle() = {
    val bengt = ExternalStore.retrieve(bengtPassword)

    val handles = UserToUserChannel.getBubbleHandle(bengt.publicKey.getEncoded)
    println("Bengt will get this key: " + Base64.getEncoder.encodeToString(handles.head.decryptSecretKey(bengt.privateKey).getEncoded))
    handles.map(bh => bengt.addBubbleHandle(bh))
    println("Bengt should get this key: " + Base64.getEncoder.encodeToString(bengt.getAllBubbleHandles.head.decryptSecretKey(bengt.privateKey).getEncoded))

    ExternalStore.save(bengt, bengtPassword)
  }

  def matsCreatesBubble_SendsItToIpfs_SendsBubbleHandleToBengt() = {
    val mats = ExternalStore.retrieve(matsPassword)

    val bubble = Bubble(bubbleName, mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val awaitedTierionResponse = Await.result(TierionClient.saveBubbleRecord(ipfsHash))   // TODO: Timeout ???e

    for (f <- mats.friends) {
      val handle = BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey, awaitedTierionResponse)
      UserToUserChannel.sendBubbleHandle(f.encodedPublicKeyOfFriend, handle)
    }

    println("Mats sent this key: " + Base64.getEncoder.encodeToString(bubbleEncryptionKey.getEncoded))
    mats.addBubbleHandle(BubbleHandle(ipfsHash, bubbleEncryptionKey, mats.publicKey, awaitedTierionResponse))

    ExternalStore.save(mats, matsPassword)
  }

  def bengtGetsBubble_ChangesIt_SavesToIpfs_SendsBubbleHandleToMats() = {
    val bengt = ExternalStore.retrieve(bengtPassword)

    val oldHandle = bengt.getAllBubbleHandles.toSet.head
    val bubbleEncryptionKey = oldHandle.decryptSecretKey(bengt.privateKey)
    val oldBubble = IPFSProxy.receive(oldHandle.ipfsHash, bubbleEncryptionKey)
    val newBubble = oldBubble.copy(name = "Changed " + oldBubble.name)
    val newIpfsHash = IPFSProxy.send(newBubble, bubbleEncryptionKey)
    val awaitedTierionResponse = Await.result(TierionClient.saveBubbleRecord(newIpfsHash))   // TODO: Timeout ???

    for (f <- bengt.friends) {
      val handle = BubbleHandle(newIpfsHash, bubbleEncryptionKey, f.publicKey, awaitedTierionResponse)
      UserToUserChannel.sendBubbleHandle(f.encodedPublicKeyOfFriend, handle)
    }

    bengt.addBubbleHandle(BubbleHandle(newIpfsHash, bubbleEncryptionKey, bengt.publicKey, awaitedTierionResponse))

    ExternalStore.save(bengt, bengtPassword)
  }

  def matsGetsBubbleHandle() = {
    val mats = ExternalStore.retrieve(matsPassword)

    val handles = UserToUserChannel.getBubbleHandle(mats.publicKey.getEncoded)
    handles.map(bh => mats.addBubbleHandle(bh))
    mats.realNumberOfUniqueBubbles should be(1)

    ExternalStore.save(mats, matsPassword)
  }

  def matsGetsBubble_CanSeeThatItIsChanged() = {
    val mats = ExternalStore.retrieve(matsPassword)

    mats.getAllBubbleHandles.toSet.size should be(2)
    mats.realNumberOfUniqueBubbles should be(1)

    val aHandle = mats.getAllBubbleHandles.toSet.head
    val latestHandle = mats.getLatestBubbleHandle(aHandle)
    val bubbleEncryptionKey = latestHandle.decryptSecretKey(mats.privateKey)
    val newBubble = IPFSProxy.receive(latestHandle.ipfsHash, bubbleEncryptionKey)
    newBubble.creatorName should be(mats.name)
    newBubble.name should be("Changed " + bubbleName)

    ExternalStore.save(mats, matsPassword)
  }
}
