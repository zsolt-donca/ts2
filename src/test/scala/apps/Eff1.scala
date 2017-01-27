package apps

import org.atnos.eff.Eff

object Eff1 extends App{
  // to create the effect
  import org.atnos.eff.option._

  // to access the runOption method
  import org.atnos.eff.syntax.option._

  val res = Eff.run(fromOption(Option(1)).runOption)
  println(res)
}
