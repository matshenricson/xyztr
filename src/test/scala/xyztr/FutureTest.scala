package xyztr

import com.twitter.util.{Await, Future}
import org.scalatest.{FlatSpec, Matchers}

class FutureTest extends FlatSpec with Matchers {
  "Futures of Some" can "be handled in one way or the other" in {
    val f1 = Future(Some(1))
    Await.result(f1.onSuccess {
      option => if (option.isDefined) option.get should be(1)
    })

    val f2 = Future(Some(2))
    Await.result(f2.onSuccess { option => option.isDefined match {
        case false => throw new IllegalStateException("Should not happen")
        case true  => option.get should be(2)
      }
    })

    val f7 = Future(Some(7))
    Await.result(f7.onSuccess {option =>
      for {
        i <- option
      } yield i should be(7)
    })

    Future(Some(8)) map { option =>
      for {
        i <- option
      } yield i should be(8)
    }
  }

  "Futures of None" can "also be handled in one way or the other" in {
    val f1 = Future(None)
    Await.result(f1.onSuccess {
      option => if (option.isDefined) throw new IllegalStateException("Very weird")
    })

    val f2 = Future(None)
    Await.result(f2.onSuccess {option =>
      for {
        i <- option
      } yield throw new IllegalStateException("Utterly weird")
    })

    Future(None) map { option =>
      for {
        i <- option
      } yield throw new IllegalStateException("Extremely weird")
    }
  }
}
