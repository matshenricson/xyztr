package xyztr

/**
  * Represents all data in a bubble.
  */
class Bubble(val name: String, creator: User) {
  def hashOfHashes() = {
    Hasher.base58HashFromBytes(name.getBytes("UTF-8"))
  }

  def addUser() = {

  }
}
