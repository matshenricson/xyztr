package xyztr.hash

import java.security.MessageDigest
import java.util.Base64

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.Stack

class SimpleHashesTest extends FlatSpec with Matchers {
  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be (2)
    stack.pop() should be (1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }

  "MessageDigest" should "generate correct hashes" in {
    val md = MessageDigest.getInstance("SHA-256")
    val hello = "Hello World!"
    md.update(hello.getBytes("UTF-8"))
    val digest = md.digest()
    val formatted = String.format("%064x", new java.math.BigInteger(1, digest))
    println("Formatted: " + formatted)

    val outEncoded = Base64.getEncoder.encodeToString(digest)
    println("Base64 Encoded: " + outEncoded)
    // Should be: QmfM2r8seH2GiRaC4esTjeraXEachRt8ZsSeGaWTPLyMoG
  }
}
