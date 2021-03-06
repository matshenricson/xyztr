package xyztr

/**
  * Fake proxy for how users communicate, however they do that. I serialize/deserialize all objects before saving them, to test that it works.
  * The to-"address" is the encoded public key of a user.
  */
object UserToUserChannel {
  val friendRequests = new scala.collection.mutable.HashMap[String, FriendRequest]()   // TODO: String is bad as key, but sender knows no public key
  val friendResponses = new scala.collection.mutable.HashMap[Set[Byte], FriendResponse]()
  val bubbleHandles = new scala.collection.mutable.HashMap[Set[Byte], BubbleHandle]()

  def sendFriendRequest(to: String, request: FriendRequest) = friendRequests.put(to, JSON.fromJsonString[FriendRequest](JSON.toJsonString(request)))
  def getFriendRequest(to: String) = friendRequests.get(to)                 // TODO: Should return a collection of FriendRequest

  def sendFriendResponse(to: Array[Byte], response: FriendResponse) = friendResponses.put(to.toSet, JSON.fromJsonString[FriendResponse](JSON.toJsonString(response)))
  def getFriendResponse(to: Array[Byte]) = friendResponses.get(to.toSet)    // TODO: Should return a collection of FriendResponse

  def sendBubbleHandle(to: Array[Byte], handle: BubbleHandle) = bubbleHandles.put(to.toSet, JSON.fromJsonString[BubbleHandle](JSON.toJsonString(handle)))
  def getBubbleHandle(to: Array[Byte]) = bubbleHandles.get(to.toSet)        // TODO: Should return a collection of BubbleHandle
}
