package xyztr

/**
  * Represents all data in a bubble.
  */
class Bubble(val name: String, creator: User) {
  val friends = new scala.collection.mutable.HashSet[Friend]()
  val encryptionKey = Crypto.createSymmetricEncryptionKey()

  def hashOfBytes() = Hasher.base58HashFromBytes(allDataAsBytes())

  // TODO: This is just a short term hack, we need to do something else, since we can't serialize/unserialize like this
  def allDataAsBytes(): Array[Byte] =
    List(name.getBytes("UTF-8"),
//         friends.map(f=> List(f.name.getBytes("UTF-8"), f.publicKey.getEncoded)),
         encryptionKey.getEncoded)
    .flatten.toArray

  def addFriend(f: Friend) = {
    friends.add(f)
  }

  // TODO: Replace name with IPFS hash
  def createBubbleInvitations() = friends.map(f => BubbleInvitation(name, Crypto.encryptWithPublicKey(encryptionKey.getEncoded, f.publicKey)))

  def hasMember(friend: Friend) = friends.exists(_.publicKey == friend.publicKey)
}

case class BubbleInvitation(bubbleName: String, encryptedEncryptionKey: Array[Byte])   // TODO: Replace bubble name with IPFS hash
