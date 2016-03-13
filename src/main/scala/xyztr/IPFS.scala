package xyztr

import javax.crypto.SecretKey

/**
  * Proxy to IPFS
  */
object IPFS {
  val storage = new scala.collection.mutable.HashMap[String, Array[Byte]]()

  /**
    * Encrypts a bubble and sends it to IPFS for storage
    *
    * @param bubble the bubble to be encrypted and sent to IPFS
    * @return the IPFS content Base58 content hash of the encrypted bubble sent to IPFS
    */
  def send(bubble: Bubble): String = {
    val encryptedBubbleBytes = Crypto.encryptWithSymmetricKey(bubble.allDataAsBytes(), bubble.encryptionKey)
    val ipfsHash = bubble.hashOfBytes()     // TODO: Well, not really, but until we fix this
    storage.put(ipfsHash, encryptedBubbleBytes)
    ipfsHash
  }

  /**
    * Gets an encrypted Bubble from IPFS and decrypts it
    *
    * @param ipfsHash the IPFS Base58 hash of the encrypted Bubble we wish to get from IPFS
    * @return the decrypted Bubble object, inside an Option, or None if it doesn't exist
    */
  def receive(ipfsHash: String, aesKey: SecretKey): Option[Array[Byte]] = {
    storage.get(ipfsHash) map (bytes => Crypto.decryptWithSymmetricKey(bytes, aesKey))
  }
}
