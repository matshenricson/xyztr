package xyztr

import java.security.spec.X509EncodedKeySpec
import java.security._
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, KeyGenerator, SecretKey, SecretKeyFactory}

object Crypto {
  def toBytes(xs: Int*) = xs.map(_.toByte).toArray

  def getPublicKeyFromEncoded(encodedPublicKey: Array[Byte]) = {
    val pubKeySpec = new X509EncodedKeySpec(encodedPublicKey)     // TODO: X509 ???
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePublic(pubKeySpec)
  }

  def createNewSymmetricEncryptionKey() = KeyGenerator.getInstance("AES").generateKey()

  def createPrivatePublicPair() = {
    val keyGen = KeyPairGenerator.getInstance("RSA")   // TODO: RSA ???
    val random = new SecureRandom()
    keyGen.initialize(1024, random)                    // TODO: 1024 ???
    keyGen.generateKeyPair()
  }

  val salt = Crypto.toBytes(1, 2, 3, 4, 5, 6, 7, 8)    // TODO: Better salt ???

  def reCreateSecretKey(password: String) = {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256)     // TODO: 256 ???
    val tmp = factory.generateSecret(spec)
    new SecretKeySpec(tmp.getEncoded(), "AES")
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

  def encryptSymmetricKeyWithPublicKey(secretKey: SecretKey, publicKey: PublicKey): Array[Byte] = {
    Crypto.encryptWithPublicKey(secretKey.getEncoded, publicKey)
  }

  def decryptSymmetricKeyWithPrivateKey(encryptedEncryptionKey: Array[Byte], privateKey: PrivateKey): SecretKey = {
    val decryptedSymmetricEncryptionKeyBytes = Crypto.decryptWithPrivateKey(encryptedEncryptionKey, privateKey)
    new SecretKeySpec(decryptedSymmetricEncryptionKeyBytes, "AES")
  }
}
