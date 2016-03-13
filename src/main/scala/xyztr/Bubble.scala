package xyztr

/**
  * Represents all data in a bubble.
  */
case class Bubble(name: String) {
  def hashOfHashes() = {
    Hasher.base58HashFromBytes(name.getBytes("UTF-8"))
  }
}
