package xyztr

import java.security.PublicKey

/**
  * Represents a friend in XYZTR, i.e. a person you share bubbles with.
  * The public key is used to encrypt the symmetric key used to encrypt bubbles and possibly the communication between friends..
  */
case class Friend(name: String, encodedPublicKey: Array[Byte]) {
  def publicKey = Crypto.getPublicKeyFromEncoded(encodedPublicKey)
}

object Friend {
  def apply(name: String, publicKey: PublicKey) = new Friend(name, publicKey.getEncoded)
}

case class FriendRequest(nameOfSender: String, encodedPublicKeyOfSender: Array[Byte]) {
  def publicKeyOfSender = Crypto.getPublicKeyFromEncoded(encodedPublicKeyOfSender)
}

object FriendRequest {
  def apply(sender: User) = new FriendRequest(sender.name, sender.publicKey.getEncoded)
}

case class FriendResponse(nameOfSender: Option[String], encodedPublicKeyOfSender: Option[Array[Byte]]) {
  def publicKeyOfSender = encodedPublicKeyOfSender.map(k => Crypto.getPublicKeyFromEncoded(k))
}

object FriendResponse {
  def apply(sender: User): FriendResponse = FriendResponse(Some(sender.name), Some(sender.publicKey.getEncoded))
  def apply(): FriendResponse = new FriendResponse(None, None)    // This is a way of rejecting the friend request
}
