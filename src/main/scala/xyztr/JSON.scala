package xyztr

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, writePretty}

object JSON {
  implicit val formats = Serialization.formats(NoTypeHints)

  def toJsonString[A <: AnyRef](a: A): String = writePretty[A](a)
  def fromJsonString[A: Manifest](json: String): A = read[A](json)
}
