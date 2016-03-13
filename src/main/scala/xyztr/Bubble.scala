package xyztr

/**
  * Represents all data in a bubble.
  */
class Bubble(val name: String, creator: User) {
  val friends = new scala.collection.mutable.HashSet[Friend]()
  val encryptionKey = Crypto.createSymmetricEncryptionKey()

  def hashOfHashes() = {
    Hasher.base58HashFromBytes(name.getBytes("UTF-8"))
  }

  def addFriend(f: Friend) = {
    friends.add(f)
  }

  // TODO: Replace name with IPFS hash
  def createBubbleInvitations() = friends.map(f => BubbleInvitation(name, Crypto.encrypt(encryptionKey.getEncoded, f.publicKey)))

  def hasMember(friend: Friend) = friends.exists(_.publicKey == friend.publicKey)
}

case class BubbleInvitation(bubbleName: String, encryptedEncryptionKey: Array[Byte])   // TODO: Replace bubble name with IPFS hash
