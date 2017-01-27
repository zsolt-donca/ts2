package apps

object Eff3TransformReaders extends App {

  import org.atnos.eff._, all._
  import org.atnos.eff.syntax.all._
  import cats._
  import cats.data._

  case class Conf(host: String, port: Int)

  type ReaderPort[A] = Reader[Int, A]
  type ReaderHost[A] = Reader[String, A]
  type ReaderConf[A] = Reader[Conf, A]

  type S1 = Fx.fx2[ReaderHost, Option]
  type S2 = Fx.fx2[ReaderPort, Option]
  type SS = Fx.fx2[ReaderConf, Option]

  val readHost: Eff[S1, String] = for {
    c <- ask[S1, String]
    h <- OptionEffect.some[S1, String]("hello")
  } yield h

  val readPort: Eff[S2, String] = for {
    c <- ask[S2, Int]
    h <- OptionEffect.some[S2, String]("world")
  } yield h

  val fromHost = new (ReaderHost ~> ReaderConf) {
    def apply[X](r: ReaderHost[X]): ReaderConf[X] = Reader((c: Conf) => r.run(c.host))
  }

  val fromPort = new (ReaderPort ~> ReaderConf) {
    def apply[X](r: ReaderPort[X]): ReaderConf[X] = Reader((c: Conf) => r.run(c.port))
  }

  val action: Eff[SS, String] = for {
    s1 <- readHost.transform(fromHost)
    s2 <- readPort.transform(fromPort)
  } yield s1 + " " + s2

  val result = action.runReader(Conf("www.me.com", 8080)).runOption.run
  println(result)
}
