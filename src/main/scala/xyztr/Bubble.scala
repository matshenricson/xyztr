package xyztr

import java.security.PublicKey

/**
  * Represents all data in a bubble.
  */
class Bubble(val name: String, creator: User, private val friends: Set[Friend]) {
  val encryptionKey = Crypto.createSymmetricEncryptionKey()
  val members = friends.map(f => BubbleMember(f.name, f.publicKey)) + BubbleMember(creator.name, creator.publicKey())

  def hashOfBytes() = Hasher.base58HashFromBytes(allDataAsBytes())

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
