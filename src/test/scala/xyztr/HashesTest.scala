package xyztr

import java.security.MessageDigest

import org.scalatest.{FlatSpec, Matchers}

class HashesTest extends FlatSpec with Matchers {
  "MessageDigest" should "generate correct hashes" in {
    val md = MessageDigest.getInstance("SHA-256")
    val hello = "Hello World!"
    md.update(hello.getBytes("UTF-8"))
    val digest = md.digest()
    digest.length should be(32)
    val formatted = String.format("%064x", new java.math.BigInteger(1, digest))
    formatted should be("7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069")

    // Should be: QmfM2r8seH2GiRaC4esTjeraXEachRt8ZsSeGaWTPLyMoG
  }

  "Hasher" should "give us Base58 encoded strings from SHA256 hashes" in {
    val s = "a string"
    val base58Hash = Hasher.base58HashFromBytes(s.getBytes("UTF-8"))
    base58Hash should be("DyrKAnRAxEWTmVaHwBVzd6vEYKiA2zieTQrsKubNHNQX")
  }
}
