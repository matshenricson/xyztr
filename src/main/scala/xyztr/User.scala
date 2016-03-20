package xyztr

import java.security.{PrivateKey, PublicKey}
import java.util.Base64

import scala.collection.mutable

/**
  * Represents the user of this program. Has a name, friends and private/public keys.
  */
class User(val name: String, val privateKey: PrivateKey, val publicKey: PublicKey) {
  val friends = new scala.collection.mutable.HashSet[Friend]()                                  // TODO: Make private ???
  private val bubbleHandleMap = new mutable.HashMap[String, mutable.Set[BubbleHandle]] with mutable.MultiMap[String, BubbleHandle]

  def getAllBubbleHandles = bubbleHandleMap.values.flatten

  private def bubbleHandleMapKey(bh: BubbleHandle) = Base64.getEncoder.encodeToString(bh.decryptSecretKey(privateKey).getEncoded)

  def addBubbleHandle(bh: BubbleHandle) = bubbleHandleMap.addBinding(bubbleHandleMapKey(bh), bh)

  def getLatestBubbleHandle(bh: BubbleHandle) = (mutable.TreeSet[BubbleHandle]() ++ bubbleHandleMap.get(bubbleHandleMapKey(bh)).get.toSet).head // TODO: Make more robust

  def realNumberOfUniqueBubbles = bubbleHandleMap.keySet.size  // TODO: For debugging, remove later

  def acceptFriendRequest(fr: FriendRequest): FriendResponse = {
    friends.add(Friend(fr.nameOfSender, fr.publicKeyOfSender))
    FriendResponse(this)
  }

  def rejectFriendRequest(fr: FriendRequest): FriendResponse = FriendResponse()

  def handleFriendResponse(fr: FriendResponse) = if (fr.nameOfSender.isDefined) friends.add(Friend(fr.nameOfSender.get, fr.publicKeyOfSender.get))

  def hasFriend(encodedPublicKeyOfPerhapsFriend: Array[Byte]) = friends.exists(f => Crypto.encodedKeysAreEqual(f.encodedPublicKeyOfFriend, encodedPublicKeyOfPerhapsFriend))
}

object User {
  def apply(name: String): User = {
    val keyPair = Crypto.createPrivatePublicPair()
    new User(name, keyPair.getPrivate, keyPair.getPublic)
  }
}
