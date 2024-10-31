package fiberplugin

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.plugin.{FiberPlugin, PluginHost}
import spinal.core.fiber.{Retainer}

import scala.collection.mutable

// https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Libraries/Misc/service_plugin.html

object Simple extends App {
  val n = 8
  case class Io() extends FiberPlugin {
    // In FiberPlugin, it is better to build every hardware during setup or during build to avoid some problem
    val logic = during build new Area {
      val i = in(UInt(n bits))
      val o = out(UInt(n bits))
      o := i
    }
  }
  case class Top() extends Component {
    val io = Io()
    val host = new PluginHost()
    host.asHostOf(io)
  }
  SimConfig.compile{
    val dut = Top()
    dut
  }.doSim{ dut =>
    dut.io.logic.i #= 0
    sleep(1)
    println(s"${dut.io.logic.o.toInt}")
  }
}

object Decoder extends App {
  val n = 8

  case class IoPlugin() extends FiberPlugin {
    val logic = during build new Area {
      val i = in(UInt(n bits))
      val o = out(UInt(n bits))
    }
  }

  // set io.logic.o according to io.logic.i
  case class Decoder() extends FiberPlugin {
    val decodeList = mutable.LinkedHashMap[UInt, UInt]()

    // when i = op, set o = value
    def addDecoding(op: UInt, value: UInt) = {
      decodeList(op) = value
    }
    val elaborationLock = Retainer()
    val logic = during build new Area {
      elaborationLock.await()

      val iop = host[IoPlugin]

      iop.logic.o := 0 // default value
      for((op, value) <- decodeList) {
      //decoding logic
        when(iop.logic.i === op) {
          iop.logic.o := value
        }
      }
    }
  }

  // map 1 to 2
  case class Plugin1() extends FiberPlugin {
    val logic = during setup new Area{
      val dp = host[Decoder]
      val buildBefore = retains(dp.elaborationLock)
      awaitBuild()
      dp.addDecoding(U(1, n bits), U(2, n bits)) // i=1 -> o=2
      buildBefore.release()
    }
  }
  
  // map 2 to 1
  case class Plugin2() extends FiberPlugin {
    val logic = during setup new Area{
      val dp = host[Decoder]
      val buildBefore = retains(dp.elaborationLock)
      awaitBuild()
      dp.addDecoding(U(2, n bits), U(1, n bits)) // i=2 -> o = 1
      buildBefore.release()
    }
  }

  case class Host(plugins: Seq[FiberPlugin]) extends Area {
    val host = new PluginHost()
    host.asHostOf(plugins)
  }

  case class Top() extends Component {
    val plugins = mutable.ArrayBuffer[FiberPlugin]()
    val io = IoPlugin()
    plugins += io
    plugins += Decoder()
    plugins += Plugin1()
    plugins += Plugin2()

    Host(plugins)
  }

  SimConfig.compile{
    val dut = Top()
    dut
  }.doSim{ dut =>
    dut.io.logic.i #= 1
    sleep(1)
    println(s"${dut.io.logic.o.toInt}")
  }
}


