package xyztr

import java.util.Date

import org.ipfs.api.Base58

/**
  * Represents all data in a bubble.
  */
case class Bubble(name: String, creatorName: String, createdTime: Long, members: Set[BubbleMember]) extends Ordered[Bubble] {
  def compare(that: Bubble) = (this.createdTime - that.createdTime).toInt
  def hasMember(friend: Friend) = members.exists(f => f.base58EncodedPublicKey == Base58.encode(friend.publicKey.getEncoded))
}

object Bubble {
  def apply(name: String, creator: User, friends: Set[Friend]): Bubble =
    Bubble(name, creator.name, new Date().getTime,
      friends.map(f => BubbleMember(f.name, Base58.encode(f.publicKey.getEncoded))) +
        BubbleMember(creator.name, Base58.encode(creator.publicKey().getEncoded)))

}

case class BubbleInvitation(ipfsHash: String, encryptedEncryptionKey: Array[Byte])

case class BubbleMember(name: String, base58EncodedPublicKey: String)
