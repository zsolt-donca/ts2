package ord.zsd.ts2.interpreter.store

import better.files.File
import cats.data._
import cats.{Eval, ~>}
import ord.zsd.ts2.interpreter.{ReadStore, StoreOp, WriteStore}
import spray.json._

/*_*/
object StoreInterpreter {
  def interpretToState[S]: StoreOp[S, ?] ~> State[S, ?] = new (StoreOp[S, ?] ~> State[S, ?]) {
    override def apply[A](fa: StoreOp[S, A]): State[S, A] = {
      fa match {
        case ReadStore() => State.get[S].asInstanceOf[State[S, A]] // should not require casting
        case WriteStore(s) => State.set[S](s)
      }
    }
  }

  def interpretToJsonFile[S: JsonFormat](path: File)(empty: S): StoreOp[S, ?] ~> Eval = new (StoreOp[S, ?] ~> Eval) {
    override def apply[A](fa: StoreOp[S, A]): Eval[A] = {
      fa match {
        case ReadStore() => Eval.later {
          if (path.exists && path.size > 0) {
            path.contentAsString.parseJson.convertTo[S].asInstanceOf[A] // should not require casting
          } else {
            empty.asInstanceOf[A]
          }
        }

        case WriteStore(s) => Eval.later {
          path.overwrite(s.toJson.prettyPrint)
          ()
        }
      }
    }
  }

}
