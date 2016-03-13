package xyztr

import java.security.MessageDigest

import org.bitcoinj.core.Base58

object Hasher {

  /**
    * Creates a Bitcoin Base58 string out of a SHA256 encode byte array
    *
    * @param bytes UTF-8 encoded characters, i.e. you get it like this: "a string".getBytes("UTF-8")
    * @return
    */
  def base58HashFromBytes(bytes: Array[Byte]): String = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(bytes)
    Base58.encode(md.digest)
  }
}
