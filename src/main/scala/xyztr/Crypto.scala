package xyztr

import java.math.BigInteger
import java.security._
import java.security.spec.{RSAPrivateCrtKeySpec, X509EncodedKeySpec}
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, KeyGenerator, SecretKey, SecretKeyFactory}

import sun.security.rsa.RSAPrivateCrtKeyImpl

object Crypto {
  def toBytes(xs: Int*) = xs.map(_.toByte).toArray

  def getPublicKeyFromEncoded(encodedPublicKey: Array[Byte]) = {
    val pubKeySpec = new X509EncodedKeySpec(encodedPublicKey)     // TODO: X509 ???
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePublic(pubKeySpec)
  }

  def createPrivateKeyBigIntegerComponentsAsStrings(pk: PrivateKey) = {
    val spec = pk.asInstanceOf[RSAPrivateCrtKeyImpl]
    List(
      spec.getModulus.toString,          // BigInteger modulus
      spec.getPublicExponent.toString,   // BigInteger publicExponent
      spec.getPrivateExponent.toString,  // BigInteger privateExponent
      spec.getPrimeP.toString,           // BigInteger primeP
      spec.getPrimeQ.toString,           // BigInteger primeQ
      spec.getPrimeExponentP.toString,   // BigInteger primeExponentP
      spec.getPrimeExponentQ.toString,   // BigInteger primeExponentQ
      spec.getCrtCoefficient.toString)   // BigInteger crtCoefficient
  }

  def getPrivateKeyFromBigIntegers(components: Seq[BigInteger]) = {
    val pubKeySpec = new RSAPrivateCrtKeySpec(components(0), components(1), components(2), components(3), components(4), components(5), components(6), components(7))
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePrivate(pubKeySpec)
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
    val spec = new PBEKeySpec(password.toCharArray, salt, 65536, 128)     // Must use 128, can't use 256
    val tmp = factory.generateSecret(spec)
    new SecretKeySpec(tmp.getEncoded, "AES")
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

  def decryptSymmetricKeyWithPrivateKey(encryptedEncryptionKey: Array[Byte], privateKey: PrivateKey): SecretKey = {
    val decryptedSymmetricEncryptionKeyBytes = Crypto.decryptWithPrivateKey(encryptedEncryptionKey, privateKey)
    new SecretKeySpec(decryptedSymmetricEncryptionKeyBytes, "AES")
  }

  def publicKeysAreEqual(pk1: PublicKey, pk2: PublicKey): Boolean = pk1.getEncoded.toSeq == pk2.getEncoded.toSeq
  def privateKeysAreEqual(pk1: PrivateKey, pk2: PrivateKey): Boolean = pk1.getEncoded.toSeq == pk2.getEncoded.toSeq
  def secretKeysAreEqual(sk1: SecretKey, sk2: SecretKey): Boolean = sk1.getEncoded.toSeq == sk2.getEncoded.toSeq
  def encodedKeysAreEqual(ba1: Array[Byte], ba2: Array[Byte]): Boolean = ba1.toSeq == ba2.toSeq
}
