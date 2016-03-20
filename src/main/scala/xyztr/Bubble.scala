package xyztr

import java.util.Date

/**
  * Represents all data in a bubble.
  */
case class Bubble(name: String, creatorName: String, startTime: Long, stopTime: Long, members: Set[BubbleMember], bubbleType: String) extends Ordered[Bubble] {
  def compare(that: Bubble) = {
    val milliDiff = this.startTime - that.startTime
    if (milliDiff < 0) -1
    else if (milliDiff > 0) +1
    else 0
  }

  def hasMember(friend: Friend) = members.exists(m => Crypto.encodedKeysAreEqual(m.encodedPublicKey, friend.encodedPublicKeyOfFriend))
}

object Bubble {
  def apply(name: String, creator: User, friends: Set[Friend]): Bubble =
    Bubble(name,
      creator.name,
      new Date().getTime,
      0,
      friends.map(f => BubbleMember(f.friendName, f.publicKey.getEncoded)) +
        BubbleMember(creator.name, creator.publicKey.getEncoded),
      "")

  def apply(name: String, creator: User, startTime: Long, stopTime: Long, friends: Set[Friend], bubbleType: String): Bubble =
    Bubble(name,
      creator.name,
      startTime,
      stopTime,
      friends.map(f => BubbleMember(f.friendName, f.publicKey.getEncoded)) +
        BubbleMember(creator.name, creator.publicKey.getEncoded),
      bubbleType)
}

case class BubbleMember(name: String, encodedPublicKey: Array[Byte]) {
  def publicKey = Crypto.getPublicKeyFromEncoded(encodedPublicKey)
}
