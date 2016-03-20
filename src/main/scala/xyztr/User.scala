package xyztr

import java.security.{PrivateKey, PublicKey}

import scala.collection.mutable

/**
  * Represents the user of this program. Has a name, friends and private/public keys.
  */
class User(val name: String, val privateKey: PrivateKey, val publicKey: PublicKey) {
  val friends = new scala.collection.mutable.HashSet[Friend]()                                  // TODO: Make private ???
  private val bubbleHandleMap = new mutable.HashMap[Array[Byte], mutable.Set[BubbleHandle]] with mutable.MultiMap[Array[Byte], BubbleHandle]

  def getAllBubbleHandles = {
    // TODO: There MUST be a smarter way of doing this...
    val bhSet = new mutable.HashSet[BubbleHandle]
    for (bubblesSet <- bubbleHandleMap.values) {
      bubblesSet.map(bh => bhSet.add(bh))
    }
    bhSet
  }

  def addBubbleHandle(bubbleHandle: BubbleHandle) = bubbleHandleMap.addBinding(bubbleHandle.encodedEncryptedEncryptionKey, bubbleHandle)

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
