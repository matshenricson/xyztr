package xyztr

import java.security.{MessageDigest, PublicKey}

import org.ipfs.api.Base58

/**
  * Represents all data in a bubble.
  */
class Bubble(val name: String, private val creator: User, private val friends: Set[Friend]) {
  val encryptionKey = Crypto.createSymmetricEncryptionKey()   // TODO: Remove the bubble encryption key from the bubble?
  val members = friends.map(f => BubbleMember(f.name, f.publicKey)) + BubbleMember(creator.name, creator.publicKey())
  val creatorName = creator.name

  def sha256OfData() = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(allDataAsBytes())
    val digest = md.digest()

    Base58.encode(digest)
  }

  // TODO: This is just a short term hack, we need to do something else, since we can't serialize/unserialize like this
  def allDataAsBytes(): Array[Byte] =
    List(name.getBytes("UTF-8"),
         creatorName.getBytes("UTF-8"),
//         members.map(m => m.allDataAsBytes()),
         encryptionKey.getEncoded)
    .flatten.toArray

  def hasMember(friend: Friend) = members.exists(_.publicKey == friend.publicKey)
}

case class BubbleInvitation(ipfsHash: String, encryptedEncryptionKey: Array[Byte])

case class BubbleMember(name: String, publicKey: PublicKey) {
  def allDataAsBytes(): Array[Byte] = List(name.getBytes("UTF-8"), publicKey.getEncoded).flatten.toArray
}
