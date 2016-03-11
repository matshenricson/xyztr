package xyztr.util

/**
  * Created by mats on 2016-03-10.
  */
object Bytes {
  def toBytes(xs: Int*) = xs.map(_.toByte).toArray
}
