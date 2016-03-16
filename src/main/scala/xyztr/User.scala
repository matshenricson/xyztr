package xyztr

import java.security.{PublicKey, KeyPair}

/**
  * Represents the user of this program. Has a name, friends and private/public keys.
  */
class User(val name: String, keyPair: KeyPair) {
  val friends = new scala.collection.mutable.HashSet[Friend]()

  def publicKey() = keyPair.getPublic
  def privateKey() = keyPair.getPrivate    // TODO: Dangerous! Mark it as deprecated some way?

  def friendRequest(fr: FriendRequest): FriendResponse = {
    friends.add(Friend(fr.name, fr.publicKey))
    FriendResponse(name, publicKey())
  }

  def hasFriend(publicKeyOfPerhapsFriend: PublicKey) = friends.exists(_.encodedPublicKey.toSeq == publicKeyOfPerhapsFriend.getEncoded.toSeq)
}

object User {
  def apply(name: String): User = {
    new User(name, Crypto.createPrivatePublicPair())
  }

  def fromPassword(password: String) = {
    val secretKey = Crypto.reCreateSecretKey(password)
  }
}
