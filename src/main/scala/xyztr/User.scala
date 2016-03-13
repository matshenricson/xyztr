package xyztr

import java.security.KeyPair

/**
  * Represents the user of this program. Has a name and private/public keys
  */
case class User(name: String, keyPair: KeyPair) {
  def publicKey() = keyPair.getPublic
}
