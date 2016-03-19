package xyztr

import org.jboss.netty.util.CharsetUtil
import org.scalatest.{FlatSpec, Matchers}

class CryptoTest extends FlatSpec with Matchers {
  "Cipher" can "encrypt data, then decrypt" in {
    val stringToEncrypt = "Hello world"
    val aesKey = Crypto.createNewSymmetricEncryptionKey()

    val encryptedBytes = Crypto.encryptWithSymmetricKey(stringToEncrypt.getBytes(CharsetUtil.UTF_8), aesKey)
    val decryptedBytes = Crypto.decryptWithSymmetricKey(encryptedBytes, aesKey)

    val decryptedString = new String(decryptedBytes, CharsetUtil.UTF_8)
    decryptedString should be(stringToEncrypt)
  }

  "Crypto" can "recreate the same secret key from the same password" in {
    val password = "secret"

    val key1 = Crypto.reCreateSecretKey(password)
    val key2 = Crypto.reCreateSecretKey(password)

    Crypto.secretKeysAreEqual(key1, key2) shouldBe true
  }
}
