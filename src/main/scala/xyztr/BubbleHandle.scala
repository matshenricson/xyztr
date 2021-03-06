package xyztr

import java.security.{PrivateKey, PublicKey}
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

import xyztr.TierionClient.SaveBubbleRecordResponse

/**
  * Represents a handle to a bubble. With this handle all data in the bubble can be retrieved.
  */
case class BubbleHandle(ipfsHash: String, encodedEncryptedEncryptionKey: Array[Byte], created: Long, blockchainHashId: Option[String]) extends Ordered[BubbleHandle] {
  def decryptSecretKey(privateKey: PrivateKey): SecretKey = new SecretKeySpec(Crypto.decryptWithPrivateKey(encodedEncryptedEncryptionKey, privateKey), "AES")

  def compare(that: BubbleHandle) = {
    val milliDiff = that.created - this.created
    if (milliDiff < 0) -1
    else if (milliDiff > 0) +1
    else 0
  }
}

object BubbleHandle {
  def apply(ipfsHash: String, secretKey: SecretKey, publicKey: PublicKey, response: Option[SaveBubbleRecordResponse] = None): BubbleHandle = response.isDefined match {
    case false => BubbleHandle(ipfsHash, Crypto.encryptWithPublicKey(secretKey.getEncoded, publicKey), new Date().getTime, None)
    case true  => BubbleHandle(ipfsHash, Crypto.encryptWithPublicKey(secretKey.getEncoded, publicKey), response.get.timestamp, Some(response.get.id))
  }
}
