package xyztr

import java.security.MessageDigest
import java.util.Base64

object Hash {
  def sha256(bytes: Array[Byte]) = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(bytes)
    md.digest()
  }

  def sha256AsBase64(bytes: Array[Byte]) = Base64.getEncoder.encodeToString(sha256(bytes))
}
