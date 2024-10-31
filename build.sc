import os.Path
import mill._, scalalib._

object ex extends ScalaModule {
  def scalaVersion = "2.12.18"
  def spinalVersion = "1.10.2a"
  override def millSourcePath: Path = os.pwd / "examples"
  def ivyDeps = Agg(
    ivy"org.scalatest::scalatest:3.2.17",
    ivy"com.github.spinalhdl::spinalhdl-core:$spinalVersion",
    ivy"com.github.spinalhdl::spinalhdl-lib:$spinalVersion"
  )
  def scalacPluginIvyDeps = Agg(ivy"com.github.spinalhdl::spinalhdl-idsl-plugin:$spinalVersion")
}
