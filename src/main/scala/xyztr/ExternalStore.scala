package xyztr

import javax.crypto.SecretKey

/**
  * Saves core data needed to bootstrap usage of XYZTR for a user on this particular client.
  * This could be an encrypted file, or IPFS itself, but then we need a IPFS hash in some way.
  */
object ExternalStore {
  def save(data: CoreUserData, secretKey: SecretKey) = ???

  def retrieve(secretKey: SecretKey) = ???
}
