package xyztr

/**
  * Holds the data saved locally for a user. With this data, the user can get all its other XYZTR data.
  *
  * To retrieve the data below, all that is needed is a password with which we can create the secret key
  * we use to encrypt the data below.
  */
case class CoreUserData(name: String,
                        friends: Set[Friend],
                        encodedPublicKey: Array[Byte],
                        privateKeyBigIntegerComponentsAsStrings: List[String],
                        bubbleHandles: Set[BubbleHandle])

object CoreUserData {
  def apply(user: User): CoreUserData = {
    new CoreUserData(user.name,
      user.friends.toSet,
      user.publicKey.getEncoded,
      Crypto.createPrivateKeyBigIntegerComponentsAsStrings(user.privateKey),
      user.getAllBubbleHandles.toSet)
  }
}
