package xyztr

import java.io.FileOutputStream
import java.math.BigInteger
import java.nio.file.{Files, Paths}
import javax.crypto.SecretKey

import org.ipfs.api.Base58
import org.jboss.netty.util.CharsetUtil
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

/**
  * Saves core data needed to bootstrap usage of XYZTR for a user on this particular client.
  * This could be an encrypted file, or IPFS itself, but then we need a IPFS hash in some way.
  */
object ExternalStore {
  implicit val formats = Serialization.formats(NoTypeHints)

  def fileName(secretKey: SecretKey) = "/home/mats/tmp/" + Base58.encode(secretKey.getEncoded)    // TODO: Probably won't work on other machine :-)

  def save(user: User, password: String) = {
    val coreUserData = CoreUserData(user)
    val secretKey = Crypto.reCreateSecretKey(password)
    val json = writePretty(coreUserData)
    val encryptedJSONBytes = Crypto.encryptWithSymmetricKey(json.getBytes(CharsetUtil.UTF_8), secretKey)
    val fos = new FileOutputStream(fileName(secretKey))
    fos.write(encryptedJSONBytes)
    fos.close()
  }

  def retrieve(password: String) = {
    val secretKey = Crypto.reCreateSecretKey(password)
    val path = Paths.get(fileName(secretKey))
    val encryptedJsonBytes = Files.readAllBytes(path)
    val jsonBytes = Crypto.decryptWithSymmetricKey(encryptedJsonBytes, secretKey)
    val json = new String(jsonBytes, CharsetUtil.UTF_8)
    val coreUserData = read[CoreUserData](json)
    val privateKey = Crypto.getPrivateKeyFromBigIntegers(coreUserData.privateKeyBigIntegerComponentsAsStrings.toSeq.map(s => new BigInteger(s)))
    val publicKey = Crypto.getPublicKeyFromEncoded(coreUserData.encodedPublicKey)
    val recreatedUser = new User(coreUserData.name, privateKey, publicKey)
    coreUserData.bubbles.foreach(b => recreatedUser.bubbles.add(b))
    coreUserData.friends.foreach(f => recreatedUser.friends.add(f))

    recreatedUser

  }
}
