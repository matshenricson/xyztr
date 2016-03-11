package xyztr.signature

import java.security.{SecureRandom, KeyPairGenerator, Signature}

import org.scalatest.{FlatSpec, Matchers}
import xyztr.util.Bytes

class SignatureTest extends FlatSpec with Matchers {
  def createPrivatePublicPair() = {
    val keyGen = KeyPairGenerator.getInstance("DSA")
    val random = new SecureRandom()
    keyGen.initialize(1024, random)
    keyGen.generateKeyPair()
  }

  "Signatures" should "let me sign messages and verify signed messages" in {
    val keyPair = createPrivatePublicPair()
    val dsaSignature = Signature.getInstance("SHA1withDSA")
    dsaSignature.initSign(keyPair.getPrivate)
    val someData = Bytes.toBytes(1, 2, 3, 4, 5, 6, 7, 8)
    dsaSignature.update(someData)
    val signature = dsaSignature.sign()

    // Now lets verify
    dsaSignature.initVerify(keyPair.getPublic)
    dsaSignature.update(someData)
    dsaSignature.verify(signature) should be(true)
  }
}
