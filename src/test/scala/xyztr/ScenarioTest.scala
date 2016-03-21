package xyztr

import java.nio.file.{Files, Paths}

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
    matsGetsBubble_AddsSoundFile_SavesToIpfs_SendsBubbleHandleToBengt()
    bengtGetsBubbleHandle()
    bengtGetsBubble_CanSeeThatItNowHasImage()
  }

  def checkThatMatsAndBengtAreMutualFriends() = {
    val mats = LocalStore.retrieve(matsPassword)
    val bengt = LocalStore.retrieve(bengtPassword)

    mats.friends.size should be(1)
    bengt.friends.size should be(1)
    Crypto.publicKeysAreEqual(mats.friends.head.publicKey, bengt.publicKey)
    Crypto.publicKeysAreEqual(bengt.friends.head.publicKey, mats.publicKey)
    mats.friends.head.friendName should be(bengtName)
    bengt.friends.head.friendName should be(matsName)
  }

  def checkThatBothHaveTheSameBubbleHandles() = {
    val mats = LocalStore.retrieve(matsPassword)
    val bengt = LocalStore.retrieve(bengtPassword)

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
    val user = LocalStore.retrieve(password)

    val bubbleEncryptionKey = user.getAllBubbleHandles.head.decryptSecretKey(user.privateKey)
    ExternalStore.fetch(user.getAllBubbleHandles.head.ipfsHash, bubbleEncryptionKey)
  }

  def preCreateUsers() = {
    val mats = User(matsName)
    val bengt = User(bengtName)
    LocalStore.save(mats, matsPassword)
    LocalStore.save(bengt, bengtPassword)
  }

  def matsSendsFriendRequestToBengt() = {
    val mats = LocalStore.retrieve(matsPassword)

    val friendRequest = FriendRequest(mats)
    UserToUserChannel.sendFriendRequest(bengtName, friendRequest)

    LocalStore.save(mats, matsPassword)
  }

  def matsHandlesFriendResponseFromBengt() = {
    val mats = LocalStore.retrieve(matsPassword)

    val friendResponses = UserToUserChannel.getFriendResponse(mats.publicKey.getEncoded)
    friendResponses.map(fr => mats.handleFriendResponse(fr))

    LocalStore.save(mats, matsPassword)
  }

  def bengtHandlesFriendRequest_SendsBackFriendResponse() = {
    val bengt = LocalStore.retrieve(bengtPassword)

    // TODO: Rewrite below so that it can handle a collection of FriendRequest objects
    val friendRequest = UserToUserChannel.getFriendRequest(bengtName).get
    val friendResponse = bengt.acceptFriendRequest(friendRequest)
    UserToUserChannel.sendFriendResponse(friendRequest.encodedPublicKeyOfSender, friendResponse)

    LocalStore.save(bengt, bengtPassword)
  }

  def bengtGetsBubbleHandle() = {
    val bengt = LocalStore.retrieve(bengtPassword)

    val handles = UserToUserChannel.getBubbleHandle(bengt.publicKey.getEncoded)
    handles.map(bh => bengt.addBubbleHandle(bh))

    LocalStore.save(bengt, bengtPassword)
  }

  def matsCreatesBubble_SendsItToIpfs_SendsBubbleHandleToBengt() = {
    val mats = LocalStore.retrieve(matsPassword)

    val bubble = Bubble(bubbleName, mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()

    ExternalStore.saveAndStamp(bubble, bubbleEncryptionKey, mats)

    LocalStore.save(mats, matsPassword)
  }

  def bengtGetsBubble_ChangesIt_SavesToIpfs_SendsBubbleHandleToMats() = {
    val bengt = LocalStore.retrieve(bengtPassword)

    val oldHandle = bengt.getAllBubbleHandles.toSet.head
    val bubbleEncryptionKey = oldHandle.decryptSecretKey(bengt.privateKey)
    val oldBubble = ExternalStore.fetch(oldHandle.ipfsHash, bubbleEncryptionKey)
    val newBubble = oldBubble.copy(name = "Changed " + oldBubble.name)

    ExternalStore.save(newBubble, bubbleEncryptionKey, bengt)

    LocalStore.save(bengt, bengtPassword)
  }

  def matsGetsBubbleHandle() = {
    val mats = LocalStore.retrieve(matsPassword)

    val handles = UserToUserChannel.getBubbleHandle(mats.publicKey.getEncoded)
    handles.map(bh => mats.addBubbleHandle(bh))
    mats.realNumberOfUniqueBubbles should be(1)

    LocalStore.save(mats, matsPassword)
  }

  def matsGetsBubble_CanSeeThatItIsChanged() = {
    val mats = LocalStore.retrieve(matsPassword)

    mats.getAllBubbleHandles.toSet.size should be(2)
    mats.realNumberOfUniqueBubbles should be(1)

    val aHandle = mats.getAllBubbleHandles.toSet.head
    val latestHandle = mats.getLatestBubbleHandle(aHandle)
    val bubbleEncryptionKey = latestHandle.decryptSecretKey(mats.privateKey)
    val newBubble = ExternalStore.fetch(latestHandle.ipfsHash, bubbleEncryptionKey)
    newBubble.creatorName should be(mats.name)
    newBubble.name should be("Changed " + bubbleName)

    LocalStore.save(mats, matsPassword)
  }

  def getAudioData(bubble: Bubble): Set[AudioData] = {
    val fileName = "src/test/resources/pin_dropping.mp3"
    val path = Paths.get(fileName)
    val audioBytes = Files.readAllBytes(path)

    bubble.audio ++ Set(AudioData(fileName, audioBytes))
  }

  def matsGetsBubble_AddsSoundFile_SavesToIpfs_SendsBubbleHandleToBengt() = {
    val mats = LocalStore.retrieve(matsPassword)

    val oldHandle = mats.getAllBubbleHandles.toSet.head
    val bubbleEncryptionKey = oldHandle.decryptSecretKey(mats.privateKey)
    val oldBubble = ExternalStore.fetch(oldHandle.ipfsHash, bubbleEncryptionKey)
    val newAudioData = getAudioData(oldBubble)
    val newBubble = oldBubble.copy(audio = newAudioData)

    ExternalStore.save(newBubble, bubbleEncryptionKey, mats)

    LocalStore.save(mats, matsPassword)
  }

  def bengtGetsBubble_CanSeeThatItNowHasImage() = {
    val bengt = LocalStore.retrieve(bengtPassword)

    bengt.getAllBubbleHandles.toSet.size should be(3)
    bengt.realNumberOfUniqueBubbles should be(1)

    val aHandle = bengt.getAllBubbleHandles.toSet.head
    val latestHandle = bengt.getLatestBubbleHandle(aHandle)
    val bubbleEncryptionKey = latestHandle.decryptSecretKey(bengt.privateKey)
    val newBubble = ExternalStore.fetch(latestHandle.ipfsHash, bubbleEncryptionKey)

    newBubble.audio.size should be(1)
    newBubble.audio.head.data.length should be > 10000

    LocalStore.save(bengt, bengtPassword)
  }
}
