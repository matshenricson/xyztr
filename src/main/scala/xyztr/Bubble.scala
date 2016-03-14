package xyztr

import java.util.Date

import org.ipfs.api.Base58

/**
  * Represents all data in a bubble.
  */
case class Bubble(name: String, creatorName: String, startTime: Long, stoppedTime: Long, members: Set[BubbleMember], bubbleType: String) extends Ordered[Bubble] {
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

  def apply(name: String, creator: User, startTime: Long, stopTime: Long, friends: Set[Friend], bubbleType: String): Bubble =
    Bubble(name,
      creator.name,
      startTime,
      stopTime,
      friends.map(f => BubbleMember(f.name, Base58.encode(f.publicKey.getEncoded))) +
        BubbleMember(creator.name, Base58.encode(creator.publicKey().getEncoded)),
      bubbleType)
}

case class BubbleInvitation(ipfsHash: String, encryptedEncryptionKey: Array[Byte])

case class BubbleMember(name: String, base58EncodedPublicKey: String)
