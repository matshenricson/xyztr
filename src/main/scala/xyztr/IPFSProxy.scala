package xyztr

import javax.crypto.SecretKey

import org.ipfs.api.{IPFS, Multihash, NamedStreamable}
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write, writePretty}

/**
  * Proxy to IPFS
  */
object IPFSProxy {
  val ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001")

  implicit val formats = Serialization.formats(NoTypeHints)

  /**
    * Encrypts a bubble and sends it to IPFS
    *
    * @param bubble the bubble to be encrypted and sent to IPFS
    * @param bubbleEncryptionKey the symmetric key used to encrypt the bubble before sending to IPFS
    * @return the IPFS content Base58 content hash of the encrypted bubble sent to IPFS
    */
  def send(bubble: Bubble, bubbleEncryptionKey: SecretKey): String = {
    val bubbleJSONString = write(bubble)
    val bubbleJSONBytes = bubbleJSONString.getBytes("UTF-8")
    val encryptedBubbleJSONBytes = Crypto.encryptWithSymmetricKey(bubbleJSONBytes, bubbleEncryptionKey)
    val data = new NamedStreamable.ByteArrayWrapper(encryptedBubbleJSONBytes)
    val merkleNode = ipfs.add(data)
    merkleNode.hash.toBase58
  }

  /**
    * Gets an encrypted Bubble from IPFS and decrypts it
    *
    * @param ipfsHash the IPFS Base58 hash of the encrypted Bubble we wish to get from IPFS
    * @param bubbleDecryptionKey the symmetric key used to decrypt the encrypted bubble fetched from IPFS
    * @return the decrypted Bubble object
    */
  def receive(ipfsHash: String, bubbleDecryptionKey: SecretKey): Bubble = {
    val hash = Multihash.fromBase58(ipfsHash)
    val encryptedBubbleJSONBytes = ipfs.cat(hash)
    val bubbleJSONBytes = Crypto.decryptWithSymmetricKey(encryptedBubbleJSONBytes, bubbleDecryptionKey)
    val bubbleJSONString = new String(bubbleJSONBytes, "UTF-8")
    read[Bubble](bubbleJSONString)
  }

  /**
    * Sends a plaintext bubble to IPFS
    *
    * @param bubble the bubble to be encrypted and sent to IPFS
    * @return the IPFS content Base58 content hash of the bubble sent to IPFS
    */
  def send(bubble: Bubble): String = {
    val bubbleJSONString = writePretty(bubble)
    val bubbleJSONBytes = bubbleJSONString.getBytes("UTF-8")
    val data = new NamedStreamable.ByteArrayWrapper(bubbleJSONBytes)
    val merkleNode = ipfs.add(data)
    merkleNode.hash.toBase58
  }

  /**
    * Gets a plaintext Bubble from IPFS
    *
    * @param ipfsHash the IPFS Base58 hash of the plaintext Bubble we wish to get from IPFS
    * @return the Bubble object
    */
  def receive(ipfsHash: String): Bubble = {
    val hash = Multihash.fromBase58(ipfsHash)
    val bubbleJSONBytes = ipfs.cat(hash)
    val bubbleJSONString = new String(bubbleJSONBytes, "UTF-8")
    read[Bubble](bubbleJSONString)
  }
}
