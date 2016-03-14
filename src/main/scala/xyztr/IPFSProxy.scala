package xyztr

import javax.crypto.SecretKey

import org.ipfs.api.{Base58, IPFS, Multihash, NamedStreamable}

/**
  * Proxy to IPFS
  */
object IPFSProxy {
  val ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001")
//  val storage = new scala.collection.mutable.HashMap[String, Array[Byte]]()

  /**
    * Encrypts a bubble and sends it to IPFS for storage
    *
    * @param bubble the bubble to be encrypted and sent to IPFS
    * @return the IPFS content Base58 content hash of the encrypted bubble sent to IPFS
    */
  def send(bubble: Bubble): Array[Byte] = {
    val encryptedBubbleBytes = Crypto.encryptWithSymmetricKey(bubble.allDataAsBytes(), bubble.encryptionKey)
    val data = new NamedStreamable.ByteArrayWrapper(encryptedBubbleBytes)
    val merkleNode = ipfs.add(data)
    println("Merkle node: " + merkleNode.toJSON)

    val ipfsHash = merkleNode.hash.toBytes
    println("Base58 received: " + Base58.encode(ipfsHash))
  //  storage.put(ipfsHash, encryptedBubbleBytes)
    ipfsHash
  }

  /**
    * Gets an encrypted Bubble from IPFS and decrypts it
    *
    * @param ipfsHash the IPFS Base58 hash of the encrypted Bubble we wish to get from IPFS
    * @return the decrypted Bubble object, inside an Option, or None if it doesn't exist
    */
  def receive(ipfsHash: Array[Byte], aesKey: SecretKey): Array[Byte] = {
    println("Base58 to use: " + Base58.encode(ipfsHash))
    val hash = new Multihash(Multihash.Type.sha2_256, ipfsHash)
    val bytes = ipfs.get(hash)
    Crypto.decryptWithSymmetricKey(bytes, aesKey)
//    storage.get(ipfsHash) map (bytes => Crypto.decryptWithSymmetricKey(bytes, aesKey))
  }
}
