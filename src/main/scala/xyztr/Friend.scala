package xyztr

import java.security.PublicKey

/**
  * Represents a friend in XYZTR, i.e. a person you share bubbles with.
  * The public key is used to encrypt the symmetric key used to encrypt bubbles and other stuff.
  */
case class Friend(name: String, publicKey: PublicKey) {
}

// TODO: Do I really need the two classes below ????
case class FriendRequest(name: String, publicKey: PublicKey) {
}

case class FriendResponse(name: String, publicKey: PublicKey) {
}
