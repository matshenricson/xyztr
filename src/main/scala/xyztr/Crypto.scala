package xyztr

import java.security.{PublicKey, PrivateKey, KeyPairGenerator, SecureRandom}
import javax.crypto.{SecretKey, KeyGenerator, Cipher}

object Crypto {
  def createSymmetricEncryptionKey() = KeyGenerator.getInstance("AES").generateKey()

  def createPrivatePublicPair() = {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    val random = new SecureRandom()
    keyGen.initialize(1024, random)
    keyGen.generateKeyPair()
  }

  def encryptWithPublicKey(plainTextBytes: Array[Byte], publicKey: PublicKey): Array[Byte] = {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")     // TODO: Is that the correct cipher for DSA public key?
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    cipher.doFinal(plainTextBytes)
  }

  def decryptWithPrivateKey(cipherTextBytes: Array[Byte], privateKey: PrivateKey): Array[Byte] = {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")     // TODO: Is that the correct cipher for DSA private key?
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    cipher.doFinal(cipherTextBytes)
  }

  def encryptWithSymmetricKey(plainTextBytes: Array[Byte], aesKey: SecretKey): Array[Byte] = {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    cipher.doFinal(plainTextBytes)
  }

  def decryptWithSymmetricKey(cipherTextBytes: Array[Byte], aesKey: SecretKey): Array[Byte] = {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    cipher.doFinal(cipherTextBytes)
  }
}
