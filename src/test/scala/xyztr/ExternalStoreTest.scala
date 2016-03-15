package xyztr

import org.ipfs.api.Base58
import org.scalatest.{FlatSpec, Matchers}

class ExternalStoreTest extends FlatSpec with Matchers {
  "CoreUserData" can "be created in a natural way" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val fr = FriendRequest(bengt.name, bengt.publicKey())
    mats.friendRequest(fr)

    val bubble = Bubble("Bubble name", mats, mats.friends.toSet)
    val bubbleEncryptionKey = Crypto.createNewSymmetricEncryptionKey()
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val invitations = mats.friends.map(f => BubbleHandle(ipfsHash, bubbleEncryptionKey, f.publicKey))

    val coreUserData = CoreUserData(mats, Set(BubbleHandle(ipfsHash, bubbleEncryptionKey, mats.publicKey())))
    coreUserData.base58EncodedPrivateKey should be(Base58.encode(mats.privateKey().getEncoded))
    coreUserData.base58EncodedPublicKey should be(Base58.encode(mats.publicKey().getEncoded))
    coreUserData.name should be(mats.name)
    coreUserData.friends.head.publicKey.getEncoded should be(bengt.publicKey().getEncoded)
  }
}
