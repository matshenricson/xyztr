package xyztr

import java.security.{PublicKey, PrivateKey}
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

import org.ipfs.api.Base58


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

  def hasMember(friend: Friend) = members.exists(f => f.base58EncodedPublicKey == Base58.encode(friend.publicKey.getEncoded))
}

object Bubble {
  def apply(name: String, creator: User, friends: Set[Friend]): Bubble =
    Bubble(name,
      creator.name,
      new Date().getTime,
      0,
      friends.map(f => BubbleMember(f.name, Base58.encode(f.publicKey.getEncoded))) +
        BubbleMember(creator.name, Base58.encode(creator.publicKey().getEncoded)),
    "")

  def apply(name: String, creator: User, startTime: Long, stopTime: Long, friends: Set[Friend], bubbleType: String, encrypted: Boolean): Bubble =
    Bubble(name,
      creator.name,
      startTime,
      stopTime,
      friends.map(f => BubbleMember(f.name, Base58.encode(f.publicKey.getEncoded))) +
        BubbleMember(creator.name, Base58.encode(creator.publicKey().getEncoded)),
      bubbleType,
      encrypted)
}

case class BubbleHandle(ipfsHash: String, base58EncodedEncryptedEncryptionKey: Option[String] = None) {
  def isBubbleEncrypted = base58EncodedEncryptedEncryptionKey.isDefined

  def getDecryptedSymmetricEncryptionKey(privateKey: PrivateKey): Option[SecretKey] = isBubbleEncrypted match {
    case false => None
    case true  => Some(new SecretKeySpec(Crypto.decryptWithPrivateKey(Base58.decode(base58EncodedEncryptedEncryptionKey.get), privateKey), "AES"))
  }
}

object BubbleHandle {
  def apply(ipfsHash: String, secretKey: SecretKey, publicKey: PublicKey): BubbleHandle =
    BubbleHandle(ipfsHash, Some(Base58.encode(Crypto.encryptWithPublicKey(secretKey.getEncoded, publicKey))))

  def apply(ipfsHash: String): BubbleHandle = new BubbleHandle(ipfsHash)
}

case class BubbleMember(name: String, base58EncodedPublicKey: String)
