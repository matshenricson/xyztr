package xyztr

import javax.crypto.SecretKey

import org.ipfs.api.{IPFS, Multihash, NamedStreamable}

/**
  * Proxy to IPFS
  */
object IPFSProxy {
  val ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001")

  /**
    * Encrypts a bubble and sends it to IPFS for storage
    *
    * @param bubble the bubble to be encrypted and sent to IPFS
    * @param bubbleEncryptionKey the symmetric key used to encrypt the bubble before sending to IPFS
    * @return the IPFS content Base58 content hash of the encrypted bubble sent to IPFS
    */
  def send(bubble: Bubble, bubbleEncryptionKey: SecretKey): String = {
    val encryptedBubbleBytes = Crypto.encryptWithSymmetricKey(bubble.allDataAsBytes(), bubbleEncryptionKey)
    val data = new NamedStreamable.ByteArrayWrapper(encryptedBubbleBytes)
    val merkleNode = ipfs.add(data)
    merkleNode.hash.toBase58
  }

  /**
    * Gets an encrypted Bubble from IPFS and decrypts it
    *
    * @param ipfsHash the IPFS Base58 hash of the encrypted Bubble we wish to get from IPFS
    * @param bubbleEncryptionKey the symmetric key used to decrypt the encrypted bubble fetched from IPFS
    * @return the decrypted Bubble object
    */
  def receive(ipfsHash: String, bubbleEncryptionKey: SecretKey): Array[Byte] = {
    val hash = Multihash.fromBase58(ipfsHash)
    val bytes = ipfs.cat(hash)
    Crypto.decryptWithSymmetricKey(bytes, bubbleEncryptionKey)
  }
}
