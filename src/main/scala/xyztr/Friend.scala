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

// TODO: Do I really need the two classes below ????
case class FriendRequest(name: String, encodedPublicKey: Array[Byte]) {
  def publicKey = Crypto.getPublicKeyFromEncoded(encodedPublicKey)
}

object FriendRequest {
  def apply(name: String, publicKey: PublicKey) = new FriendRequest(name, publicKey.getEncoded)
}

case class FriendResponse(name: String, encodedPublicKey: Array[Byte]) {
  def publicKey = Crypto.getPublicKeyFromEncoded(encodedPublicKey)
}

object FriendResponse {
  def apply(name: String, publicKey: PublicKey) = new FriendResponse(name, publicKey.getEncoded)
  def apply() = new FriendResponse("", Array.empty[Byte])    // This is a way of rejecting the friend request
}
