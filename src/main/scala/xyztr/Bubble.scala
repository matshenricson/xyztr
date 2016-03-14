package xyztr

import java.security.{MessageDigest, PublicKey}

import org.ipfs.api.Base58

/**
  * Represents all data in a bubble.
  */
class Bubble(val name: String, val creator: User, private val friends: Set[Friend]) {
  val encryptionKey = Crypto.createSymmetricEncryptionKey()   // TODO: Remove the bubble encryption key from the bubble?
  val members = friends.map(f => BubbleMember(f.name, f.publicKey)) + BubbleMember(creator.name, creator.publicKey())

  def sha256OfData() = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(allDataAsBytes())
    val digest = md.digest()

    Base58.encode(digest)
  }

  // TODO: This is just a short term hack, we need to do something else, since we can't serialize/unserialize like this
  def allDataAsBytes(): Array[Byte] =
    List(name.getBytes("UTF-8"),
         creator.publicKey().getEncoded,
//         members.map(m => m.allDataAsBytes()),
         encryptionKey.getEncoded)
    .flatten.toArray

  def hasMember(friend: Friend) = members.exists(_.publicKey == friend.publicKey)
}

case class BubbleInvitation(ipfsHash: String, encryptedEncryptionKey: Array[Byte])

case class BubbleMember(name: String, publicKey: PublicKey) {
  def allDataAsBytes(): Array[Byte] = List(name.getBytes("UTF-8"), publicKey.getEncoded).flatten.toArray
}

object BubbleCreator {
  def create(name: String, creator: User, friends: Set[Friend]): Set[BubbleInvitation] = {
    val bubble = new Bubble(name, creator, friends)
    val ipfsHash = IPFSProxy.send(bubble)
    friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptWithPublicKey(bubble.encryptionKey.getEncoded, f.publicKey)))
  }
}
