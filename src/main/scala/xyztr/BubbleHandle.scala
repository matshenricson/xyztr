package xyztr

import java.security.{PrivateKey, PublicKey}
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
  * Represents a handle to a bubble. With this handle all data in the bubble can be retrieved.
  */
case class BubbleHandle(ipfsHash: String, encodedEncryptedEncryptionKey: Array[Byte]) {
  def decryptSecretKey(privateKey: PrivateKey): SecretKey = new SecretKeySpec(Crypto.decryptWithPrivateKey(encodedEncryptedEncryptionKey, privateKey), "AES")
}

object BubbleHandle {
  def apply(ipfsHash: String, secretKey: SecretKey, publicKey: PublicKey): BubbleHandle =
    BubbleHandle(ipfsHash, Crypto.encryptWithPublicKey(secretKey.getEncoded, publicKey))
}
