package xyztr

/**
  * Represents all data in a bubble.
  */
class Bubble(val name: String, creator: User, val friends: Set[Friend]) {    // TODO: How add new friends ?? Clone old one?
  val encryptionKey = Crypto.createSymmetricEncryptionKey()

  def hashOfBytes() = Hasher.base58HashFromBytes(allDataAsBytes())

  // TODO: This is just a short term hack, we need to do something else, since we can't serialize/unserialize like this
  def allDataAsBytes(): Array[Byte] =
    List(name.getBytes("UTF-8"),
         creator.publicKey().getEncoded,
//         friends.map(f=> List(f.name.getBytes("UTF-8"), f.publicKey.getEncoded)),
         encryptionKey.getEncoded)
    .flatten.toArray

  // TODO: Replace name with IPFS hash
  def createBubbleInvitations() = friends.map(f => BubbleInvitation(name, Crypto.encryptWithPublicKey(encryptionKey.getEncoded, f.publicKey)))

  def hasMember(friend: Friend) = friends.exists(_.publicKey == friend.publicKey)
}

case class BubbleInvitation(ipfsHash: String, encryptedEncryptionKey: Array[Byte])

object BubbleCreator {
  def create(name: String, creator: User, friends: Set[Friend]): Set[BubbleInvitation] = {
    val bubble = new Bubble(name, creator, friends)
    val ipfsHash = IPFS.send(bubble)
    friends.map(f => BubbleInvitation(ipfsHash, Crypto.encryptWithPublicKey(bubble.encryptionKey.getEncoded, f.publicKey)))
  }
}
