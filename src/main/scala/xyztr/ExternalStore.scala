package xyztr

import javax.crypto.SecretKey

import com.twitter.util.Await
import xyztr.TierionClient.SaveBubbleRecordResponse

/**
  * The external storage for bubbles
  */
object ExternalStore {
  private def sendBubbleNotificationsToBubbleMembers(bubble: Bubble, ipfsHash: String, bubbleEncryptionKey: SecretKey, user: User,
                                                     tierionResponse: Option[SaveBubbleRecordResponse] = None) = {
    for (m <- bubble.members) {
      val handle = BubbleHandle(ipfsHash, bubbleEncryptionKey, m.publicKey, tierionResponse)
      UserToUserChannel.sendBubbleHandle(m.encodedPublicKey, handle)
    }

    // Also add new BubbleHandle to yourself...
    user.addBubbleHandle(BubbleHandle(ipfsHash, bubbleEncryptionKey, user.publicKey, tierionResponse))
  }

  def save(bubble: Bubble, bubbleEncryptionKey: SecretKey, user: User) = {
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    sendBubbleNotificationsToBubbleMembers(bubble, ipfsHash, bubbleEncryptionKey, user, None)
  }

  def saveAndStamp(bubble: Bubble, bubbleEncryptionKey: SecretKey, user: User) = {
    val ipfsHash = IPFSProxy.send(bubble, bubbleEncryptionKey)
    val tierionResponse = Await.result(TierionClient.saveBubbleRecord(ipfsHash))   // TODO: Timeout ???
    sendBubbleNotificationsToBubbleMembers(bubble, ipfsHash, bubbleEncryptionKey, user, tierionResponse)
  }

  def fetch(ipfsHash: String, bubbleDecryptionKey: SecretKey): Bubble = {
    IPFSProxy.receive(ipfsHash, bubbleDecryptionKey)
  }
}
