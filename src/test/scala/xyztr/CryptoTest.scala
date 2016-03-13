package xyztr

import org.scalatest.{FlatSpec, Matchers}

class CryptoTest extends FlatSpec with Matchers {
  "Cipher" can "encrypt data, then decrypt" in {
    val stringToEncrypt = "Hello world"
    val aesKey = Crypto.createSymmetricEncryptionKey()

    val encryptedBytes = Crypto.encryptWithSymmetricKey(stringToEncrypt.getBytes("UTF-8"), aesKey)
    val decryptedBytes = Crypto.decryptWithSymmetricKey(encryptedBytes, aesKey)

    val decryptedString = new String(decryptedBytes, "UTF-8")
    decryptedString should be(stringToEncrypt)
  }
}
