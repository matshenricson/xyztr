package xyztr

/**
  * Proxy to IPFS
  */
object IPFS {
  val storage = new scala.collection.mutable.HashMap[String, Bubble]()

  /**
    * Encrypts a bubble and sends it to IPFS for storage
    *
    * @param bubble the bubble to be encrypted and sent to IPFS
    * @return the IPFS content Base58 content hash of the encrypted bubble sent to IPFS
    */
  def send(bubble: Bubble): String = {
    storage.put(bubble.hashOfBytes(), bubble)
    bubble.hashOfBytes()
  }

  /**
    * Gets an encrypted Bubble from IPFS and decrypts it
    *
    * @param ipfsHash the IPFS Base58 hash of the encrypted Bubble we wish to get from IPFS
    * @return the decrypted Bubble object, inside an Option, or None if it doesn't exist
    */
  def receive(ipfsHash: String): Option[Bubble] = {
    storage.get(ipfsHash)
  }
}
