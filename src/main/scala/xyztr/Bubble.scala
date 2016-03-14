package xyztr

import org.ipfs.api.Base58

/**
  * Represents all data in a bubble.
  */
case class Bubble(name: String, creatorName: String, members: Set[BubbleMember]) {
  def hasMember(friend: Friend) = members.exists(f => f.base58EncodedPublicKey == Base58.encode(friend.publicKey.getEncoded))
}

object BubbleCreator {
  def create(name: String, creator: User, friends: Set[Friend]) =
    Bubble(name, creator.name, friends.map(f => BubbleMember(f.name,       Base58.encode(f.publicKey.getEncoded))) +
                                                BubbleMember(creator.name, Base58.encode(creator.publicKey().getEncoded)))
}

case class BubbleInvitation(ipfsHash: String, encryptedEncryptionKey: Array[Byte])

case class BubbleMember(name: String, base58EncodedPublicKey: String)
