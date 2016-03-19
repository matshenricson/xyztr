package xyztr

import org.scalatest.{FlatSpec, Matchers}

class UserToUserChannelTest extends FlatSpec with Matchers {
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
    val sentBubbleHandle = BubbleHandle("SomeIpfsHash", Crypto.createNewSymmetricEncryptionKey(), friend.publicKey, None)
    UserToUserChannel.sendBubbleHandle(friend.encodedPublicKeyOfFriend, sentBubbleHandle)

    // Bengt gets the BubbleHandle and can decrypt the symmetric key
    val receivedBubbleHandle = UserToUserChannel.getBubbleHandle(bengt.publicKey.getEncoded).getOrElse(throw new IllegalStateException("What?"))
    val decryptedSymmetricKey = receivedBubbleHandle.decryptSecretKey(bengt.privateKey)
    decryptedSymmetricKey.getAlgorithm should be("AES")
  }
}
