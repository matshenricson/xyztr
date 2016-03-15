package xyztr

import java.security.Signature

import org.scalatest.{FlatSpec, Matchers}

class SignatureTest extends FlatSpec with Matchers {
  "Signatures" should "let me sign messages and verify signed messages" in {
    val keyPair = Crypto.createPrivatePublicPair()
    val dsaSignature = Signature.getInstance("SHA1withRSA")
    dsaSignature.initSign(keyPair.getPrivate)
    val someData = Crypto.toBytes(1, 2, 3, 4, 5, 6, 7, 8)
    dsaSignature.update(someData)
    val signature = dsaSignature.sign()

    // Now lets verify
    dsaSignature.initVerify(keyPair.getPublic)
    dsaSignature.update(someData)
    dsaSignature.verify(signature) should be(true)
  }
}
