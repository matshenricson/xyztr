package xyztr

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

/**
  * Fake proxy for how users communicate, however they do that. I serialize/deserialize all objects before saving them, to test that it works.
  * The to-"address" is the encoded public key of a user.
  */
object UserToUserChannel {
  val friendRequests = new scala.collection.mutable.HashMap[String, FriendRequest]()   // TODO: String is bad as key, but sender knows no public key
  val friendResponses = new scala.collection.mutable.HashMap[Set[Byte], FriendResponse]()
  val bubbleHandles = new scala.collection.mutable.HashMap[Set[Byte], BubbleHandle]()

  implicit val formats = Serialization.formats(NoTypeHints)

  def sendFriendRequest(to: String, request: FriendRequest) = friendRequests.put(to, read[FriendRequest](write(request)))
  def getFriendRequest(to: String) = friendRequests.get(to)

  def sendFriendResponse(to: Array[Byte], response: FriendResponse) = friendResponses.put(to.toSet, read[FriendResponse](write(response)))
  def getFriendResponse(to: Array[Byte]) = friendResponses.get(to.toSet)

  def sendBubbleHandle(to: Array[Byte], handle: BubbleHandle) = bubbleHandles.put(to.toSet, read[BubbleHandle](write(handle)))
  def getBubbleHandle(to: Array[Byte]) = bubbleHandles.get(to.toSet)
}
