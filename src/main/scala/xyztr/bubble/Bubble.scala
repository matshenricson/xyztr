package xyztr.bubble

import xyztr.hash.Hasher

/**
  * Created by mats on 2016-03-12.
  */
case class Bubble(name: String) {
  def hashOfHashes() = {
    Hasher.base58HashFromBytes(name.getBytes("UTF-8"))
  }
}
