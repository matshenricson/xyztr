package xyztr

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.scalatest.{FlatSpec, Matchers}

class UserToUserChannelTest extends FlatSpec with Matchers {
  implicit val formats = Serialization.formats(NoTypeHints)

  "FriendRequest" can "be sent to someone" in {
    val mats = User("Mats Henricson")

    // Mats sends a friend request to Bengt
    val friendRequestToBengt = FriendRequest(mats)
    UserToUserChannel.sendFriendRequest("Bengt Henricson", friendRequestToBengt)

    // Bengt gets the friend request from Mats
    val bengt = User("Bengt Henricson")
    val receivedFriendRequest = UserToUserChannel.getFriendRequest(bengt.name).get

    bengt.acceptFriendRequest(receivedFriendRequest)

    // Mats should now be Bengts friend
    bengt.hasFriend(mats.publicKey.getEncoded)
  }

  "FriendResponse" can "be sent to someone" in {
    val mats = User("Mats Henricson")

    // Mats sends a friend request to Bengt
    val friendRequestToBengt = FriendRequest(mats)
    UserToUserChannel.sendFriendRequest("Bengt Henricson", friendRequestToBengt)

    // Bengt gets the friend request from Mats
    val bengt = User("Bengt Henricson")
    val receivedFriendRequest = UserToUserChannel.getFriendRequest(bengt.name).get

    val response = bengt.acceptFriendRequest(receivedFriendRequest)

    // Bengt sends the response back to Mats
    UserToUserChannel.sendFriendResponse(receivedFriendRequest.encodedPublicKeyOfSender, response)
    val responseFromBengt = UserToUserChannel.getFriendResponse(mats.publicKey.getEncoded).get
    mats.handleFriendResponse(responseFromBengt)

    // Bengt should now be Mats friend
    bengt.hasFriend(mats.publicKey.getEncoded)
  }

  "BubbleHandle" can "be sent to a friend" in {
    val mats = User("Mats Henricson")
    val bengt = User("Bengt Henricson")

    val receivedFriendRequestFromBengt = FriendRequest(bengt)
    mats.acceptFriendRequest(receivedFriendRequestFromBengt)   // Bengt is now Mats Friend

    // Mats sends a BubbleHandle to Bengt
    val friend = mats.friends.head
    val sentBubbleHandle = BubbleHandle("SomeIpfsHash", Crypto.createNewSymmetricEncryptionKey(), friend.publicKey)
    UserToUserChannel.sendBubbleHandle(friend.encodedPublicKey, sentBubbleHandle)

    // Bengt gets the BubbleHandle and can decrypt the symmetric key
    val receivedBubbleHandle = UserToUserChannel.getBubbleHandle(bengt.publicKey.getEncoded).getOrElse(throw new IllegalStateException("What?"))
    val decryptedSymmetricKey = receivedBubbleHandle.decryptSecretKey(bengt.privateKey)
    decryptedSymmetricKey.get.getAlgorithm should be("AES")
  }
}
