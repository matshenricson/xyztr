package xyztr

import java.util.Date

import org.jboss.netty.util.CharsetUtil

/**
  * Represents all data in a bubble.
  */
case class Bubble(name: String, creatorName: String, startTime: Long, stopTime: Long, members: Set[BubbleMember], bubbleType: String) extends Ordered[Bubble] {
  private def allDataAsBytes: Array[Byte] = JSON.toJsonString(this).getBytes(CharsetUtil.UTF_8)

  def compare(that: Bubble) = {
    val milliDiff = this.startTime - that.startTime
    if (milliDiff < 0) -1
    else if (milliDiff > 0) +1
    else 0
  }

  def hasMember(friend: Friend) = members.exists(m => Crypto.encodedKeysAreEqual(m.encodedPublicKey, friend.encodedPublicKeyOfFriend))
  def sha256AsBase64: String = Hash.sha256AsBase64(allDataAsBytes)
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
