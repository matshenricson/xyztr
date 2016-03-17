package xyztr

import java.security.{PrivateKey, PublicKey}
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
  * Represents all data in a bubble.
  */
case class Bubble(name: String, creatorName: String, startTime: Long, stopTime: Long, members: Set[BubbleMember], bubbleType: String, encrypted: Boolean = true) extends Ordered[Bubble] {
  def compare(that: Bubble) = {
    val milliDiff = this.startTime - that.startTime
    if (milliDiff < 0) -1
    else if (milliDiff > 0) +1
    else 0
  }

  def hasMember(friend: Friend) = members.exists(m => Crypto.encodedKeysAreEqual(m.encodedPublicKey, friend.encodedPublicKey))
}

object Bubble {
  def apply(name: String, creator: User, friends: Set[Friend]): Bubble =
    Bubble(name,
      creator.name,
      new Date().getTime,
      0,
      friends.map(f => BubbleMember(f.name, f.publicKey.getEncoded)) +
        BubbleMember(creator.name, creator.publicKey.getEncoded),
      "")

  def apply(name: String, creator: User, startTime: Long, stopTime: Long, friends: Set[Friend], bubbleType: String, encrypted: Boolean): Bubble =
    Bubble(name,
      creator.name,
      startTime,
      stopTime,
      friends.map(f => BubbleMember(f.name, f.publicKey.getEncoded)) +
        BubbleMember(creator.name, creator.publicKey.getEncoded),
      bubbleType,
      encrypted)
}

case class BubbleHandle(ipfsHash: String, encodedEncryptedEncryptionKey: Option[Array[Byte]] = None) {
  def isBubbleEncrypted = encodedEncryptedEncryptionKey.isDefined

  def decryptSecretKey(privateKey: PrivateKey): Option[SecretKey] = isBubbleEncrypted match {
    case false => None
    case true  => Some(new SecretKeySpec(Crypto.decryptWithPrivateKey(encodedEncryptedEncryptionKey.get, privateKey), "AES"))
  }
}

object BubbleHandle {
  def apply(ipfsHash: String, secretKey: SecretKey, publicKey: PublicKey): BubbleHandle =
    BubbleHandle(ipfsHash, Some(Crypto.encryptWithPublicKey(secretKey.getEncoded, publicKey)))

  def apply(ipfsHash: String): BubbleHandle = new BubbleHandle(ipfsHash)
}

case class BubbleMember(name: String, encodedPublicKey: Array[Byte]) {
  def publicKey = Crypto.getPublicKeyFromEncoded(encodedPublicKey)
}
