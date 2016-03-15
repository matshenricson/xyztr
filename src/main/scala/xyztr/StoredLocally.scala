package xyztr

/**
  * Holds the data saved locally for a user. With this data, the user can get all its other XYZTR data.
  *
  * To retrieve the data below, all that is needed is a password with which we can create the secret key
  * we use to encrypt the data below.
  */
case class StoredLocally(name: String,
                         friends: Set[Friend],
                         base58EncodedPublicKey: String,
                         base58EncodedPrivateKey: String,
                         bubbles: Set[BubbleHandle])
