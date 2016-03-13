package xyztr

import java.security.{PublicKey, PrivateKey, KeyPairGenerator, SecureRandom}
import javax.crypto.Cipher

object Crypto {
  def createPrivatePublicPair() = {
    val keyGen = KeyPairGenerator.getInstance("DSA")
    val random = new SecureRandom()
    keyGen.initialize(1024, random)
    keyGen.generateKeyPair()
  }

  def encrypt(plainTextBytes: Array[Byte], publicKey: PublicKey): Array[Byte] = {
    val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")     // TODO: Is that the correct cipher for DSA public key?
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    cipher.doFinal(plainTextBytes)
  }

  def decrypt(cipherTextBytes: Array[Byte], privateKey: PrivateKey): Array[Byte] = {
    val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")     // TODO: Is that the correct cipher for DSA private key?
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    cipher.doFinal(cipherTextBytes)
  }
}
