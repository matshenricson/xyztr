package xyztr

import javax.crypto.{Cipher, KeyGenerator}

import org.scalatest.{FlatSpec, Matchers}

class CryptoTest extends FlatSpec with Matchers {
  "Cipher" can "encrypt data, then decrypt" in {
    val stringToEncrypt = "Hello world"
    val aesKey = KeyGenerator.getInstance("AES").generateKey()
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

    // Lets encrypt
    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    val cipherText = cipher.doFinal(stringToEncrypt.getBytes("UTF-8"))

    // Lets decrypt
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    val decryptedByteArray = cipher.doFinal(cipherText)
    val decryptedString = new String(decryptedByteArray, "UTF-8")
    decryptedString should be(stringToEncrypt)
  }
}
