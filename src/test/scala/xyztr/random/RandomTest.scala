package xyztr.random

import java.security.SecureRandom
import java.util.Random

import org.scalatest.{FlatSpec, Matchers}
import xyztr.util.Bytes

class RandomTest extends FlatSpec with Matchers {
  "SecureRandom" should "generate completely random numbers by default" in {
    val rg = new SecureRandom()
    val randomLong = rg.nextLong
    randomLong should not be 606409227870597303L
  }

  "SecureRandom" should "generate completely random numbers even if the same seed is used" in {
    val seed = new String("seed").getBytes("UTF-8")
    val rg1 = new SecureRandom()
    val rg2 = new SecureRandom()
    rg1.setSeed(seed)
    rg2.setSeed(seed)

    val b1 = Bytes.toBytes(1, 2, 3, 4, 5, 6, 7, 8)
    val b2 = Bytes.toBytes(1, 2, 3, 4, 5, 6, 7, 8)
    rg1.nextBytes(b1)
    rg2.nextBytes(b2)
    b1 should not be b2
  }

  "Random" should "generate completely non-random numbers if the same seed is used" in {
    val seed = 77
    val rg1 = new Random(seed)
    val rg2 = new Random(seed)

    val b1 = Bytes.toBytes(1, 2, 3, 4, 5, 6, 7, 8)
    val b2 = Bytes.toBytes(1, 2, 3, 4, 5, 6, 7, 8)
    rg1.nextBytes(b1)
    rg2.nextBytes(b2)
    b1 should be(b2)
  }
}
