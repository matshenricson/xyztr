package xyztr

import java.security.{KeyPairGenerator, SecureRandom}

object KeyGen {
  def createPrivatePublicPair() = {
    val keyGen = KeyPairGenerator.getInstance("DSA")
    val random = new SecureRandom()
    keyGen.initialize(1024, random)
    keyGen.generateKeyPair()
  }
}
