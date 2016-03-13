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

  def addFriend(friend: Friend) = {
    friends.add(friend)
    BubbleRequest(name, Crypto.encrypt(encryptionKey.getEncoded, friend.publicKey))
  }

  def hasMember(friend: Friend) = friends.exists(_.publicKey == friend.publicKey)
}

case class BubbleRequest(bubbleName: String, encryptedEncryptionKey: Array[Byte])
