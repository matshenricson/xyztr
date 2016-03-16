package xyztr

import java.security.PublicKey

/**
  * Represents a friend in XYZTR, i.e. a person you share bubbles with.
  * The public key is used to encrypt the symmetric key used to encrypt bubbles and other stuff.
  */
case class Friend(name: String, encodedPublicKey: Array[Byte]) {
  def publicKey = Crypto.getPublicKeyFromEncoded(encodedPublicKey)
}

object Friend {
  def apply(name: String, publicKey: PublicKey) = new Friend(name, publicKey.getEncoded)
}

// TODO: Do I really need the two classes below ????
case class FriendRequest(name: String, publicKey: PublicKey) {
}

case class FriendResponse(name: String, publicKey: PublicKey) {
}
