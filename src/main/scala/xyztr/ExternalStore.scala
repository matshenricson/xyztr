package xyztr

import java.io.FileOutputStream
import java.nio.file.{Files, Paths}
import javax.crypto.SecretKey

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

/**
  * Saves core data needed to bootstrap usage of XYZTR for a user on this particular client.
  * This could be an encrypted file, or IPFS itself, but then we need a IPFS hash in some way.
  */
object ExternalStore {
  val fileName = "/home/mats/tmp/userData.txt"    // TODO: Probably won't work on other machine :-)
  implicit val formats = Serialization.formats(NoTypeHints)

  def save(coreUserData: CoreUserData, secretKey: SecretKey) = {
    val json = writePretty(coreUserData)
    val encryptedJSONBytes = Crypto.encryptWithSymmetricKey(json.getBytes("UTF-8"), secretKey)
    val fos = new FileOutputStream(fileName)
    fos.write(encryptedJSONBytes)
    fos.close()
  }

  def retrieve(secretKey: SecretKey) = {
    val path = Paths.get(fileName)
    val encryptedJsonBytes = Files.readAllBytes(path)
    val jsonBytes = Crypto.decryptWithSymmetricKey(encryptedJsonBytes, secretKey)
    val json = new String(jsonBytes, "UTF-8")
    read[CoreUserData](json)
  }
}
