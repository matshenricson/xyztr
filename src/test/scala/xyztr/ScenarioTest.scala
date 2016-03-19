package xyztr

import com.twitter.util.Await
import org.scalatest.{FlatSpec, Matchers}

class ScenarioTest extends FlatSpec with Matchers {
  val matsPassword = "matspassword"
  val bengtPassword = "bengtspassword"
  val matsName = "Mats Henricson"
  val bengtName = "Bengt Henricson"

  "Abstractions" can "easily be used as intended" in {
    preCreateUsers()
    matsSendsFriendRequestToBengt()
    bengtHandlesFriendRequestAndSendsBackFriendResponse()
    matsHandlesFriendResponseFromBengt()
    matsCreatesBubbleSendsItToIpfsAndSendsBubbleHandleToBengt()
    bengtHandlesBubbleHandle()
    checkThatMatsAndBengtAreMutualFriends()
    checkThatBothHaveTheSameBubbleHandles()
    checkThatBothCanDecryptToTheSameBubble()
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

    mats.bubblesMap.values.size should be(1)
    bengt.bubblesMap.values.size should be(1)
    mats.bubblesMap.values.head.ipfsHash should be(bengt.bubblesMap.values.head.ipfsHash)
    Crypto.secretKeysAreEqual(mats.bubblesMap.values.head.decryptSecretKey(mats.privateKey), bengt.bubblesMap.values.head.decryptSecretKey(bengt.privateKey))
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
    val bubbleEncryptionKey = user.bubblesMap.values.head.decryptSecretKey(user.privateKey)
    IPFSProxy.receive(user.bubblesMap.values.head.ipfsHash, bubbleEncryptionKey)
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
    UserToUserChannel.sendFriendRequest("Bengt Henricson", friendRequest)

    ExternalStore.save(mats, matsPassword)
  }

  def matsHandlesFriendResponseFromBengt() = {
    val mats = ExternalStore.retrieve(matsPassword)

    val friendResponses = UserToUserChannel.getFriendResponse(mats.publicKey.getEncoded)
    friendResponses.map(fr => mats.handleFriendResponse(fr))

    ExternalStore.save(mats, matsPassword)
  }

  def bengtHandlesFriendRequestAndSendsBackFriendResponse() = {
    val bengt = ExternalStore.retrieve(bengtPassword)

    // TODO: Rewrite below so that it can handle a collection of FriendRequest objects
    val friendRequest = UserToUserChannel.getFriendRequest(bengtName).get
    val friendResponse = bengt.acceptFriendRequest(friendRequest)
    UserToUserChannel.sendFriendResponse(friendRequest.encodedPublicKeyOfSender, friendResponse)

    ExternalStore.save(bengt, bengtPassword)
  }

  def bengtHandlesBubbleHandle() = {
    val bengt = ExternalStore.retrieve(bengtPassword)

    val handles = UserToUserChannel.getBubbleHandle(bengt.publicKey.getEncoded)
    handles.map(h => bengt.addBubble(h))

    ExternalStore.save(bengt, bengtPassword)
  }

  def matsCreatesBubbleSendsItToIpfsAndSendsBubbleHandleToBengt() = {
    val mats = ExternalStore.retrieve(matsPassword)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val response = TierionClient.saveBubbleRecord(bubble.sha256AsBase64)

    for (f <- mats.friends) {
      val handle = BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey, Await.result(response))   // TODO: Timeout ???e
      UserToUserChannel.sendBubbleHandle(f.encodedPublicKeyOfFriend, handle)
    }

    mats.addBubble(BubbleHandle(ipfsHash, bubbleEncryptionKey, mats.publicKey))

    ExternalStore.save(mats, matsPassword)
  }
}
