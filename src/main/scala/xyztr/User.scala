package xyztr

import java.security.{PrivateKey, PublicKey}

/**
  * Represents the user of this program. Has a name, friends and private/public keys.
  */
class User(val name: String, val privateKey: PrivateKey, val publicKey: PublicKey) {
  val friends = new scala.collection.mutable.HashSet[Friend]()
  val bubbles = new scala.collection.mutable.HashSet[BubbleHandle]()

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
