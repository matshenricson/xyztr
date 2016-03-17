package xyztr

import org.scalatest.{FlatSpec, Matchers}

class ScenarioTest extends FlatSpec with Matchers {
  val matsPassword = "matspassword"
  val bengtsPassword = "bengtspassword"

  "Xxxxxxx" can "YYYYYYYYYYYYYYYYYYYY" in {
    {
      val firstMats = User("Mats Henricson")
      saveUserToFile(firstMats, matsPassword)
    }

    val mats = fetchUserFromFile(matsPassword)
  }

  def saveUserToFile(user: User, password: String) = ExternalStore.save(user, password)
  def fetchUserFromFile(password: String) = ExternalStore.retrieve(password)
}
