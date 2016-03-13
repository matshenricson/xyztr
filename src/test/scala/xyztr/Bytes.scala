package xyztr

/**
  * Utility functions for bytes
  */
object Bytes {
  def toBytes(xs: Int*) = xs.map(_.toByte).toArray
}
