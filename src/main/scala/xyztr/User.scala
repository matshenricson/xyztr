package xyztr

import java.security.KeyPair

/**
  * Represents the user of this program. Has a name, friends and private/public keys.
  */
class User(val name: String, keyPair: KeyPair) {
  val friends = new scala.collection.mutable.HashSet[Friend]()

  def publicKey() = keyPair.getPublic
  def privateKey() = keyPair.getPrivate    // TODO: Dangerous! Mark it as deprecated some way?

  def acceptFriendRequest(fr: FriendRequest): FriendResponse = {
    friends.add(Friend(fr.nameOfSender, fr.publicKeyOfSender))
    FriendResponse(this)
  }

  def rejectFriendRequest(fr: FriendRequest): FriendResponse = FriendResponse()

  def handleFriendResponse(fr: FriendResponse) = {
    if (fr.nameOfSender.isDefined) friends.add(Friend(fr.nameOfSender.get, fr.publicKeyOfSender.get))
  }

  def hasFriend(encodedPublicKeyOfPerhapsFriend: Array[Byte]) = friends.exists(_.encodedPublicKey.toSeq == encodedPublicKeyOfPerhapsFriend.toSeq)
}

object User {
  def apply(name: String): User = {
    new User(name, Crypto.createPrivatePublicPair())
  }

  def fromPassword(password: String) = {
    val secretKey = Crypto.reCreateSecretKey(password)
    // TODO: Now use this to fetch from external store
  }
}
