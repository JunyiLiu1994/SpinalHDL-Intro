package stream

import spinal.core._
import spinal.core.sim._
import spinal.lib._

object MasterSlave extends App {
  // https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Data%20types/bundle.html#master-slave
  case class MyInterface() extends Bundle with IMasterSlave {
    val data1 = Bool() // master to slave
    val data2 = Bool() // slave to master

    def asMaster() {
      out(data1)
      in(data2)
    }
  }

  case class Top() extends Component {
    val up = slave(MyInterface())
    val down = master(MyInterface())

    down.data1 := up.data1
    up.data2 := down.data2
  }

  SpinalVerilog(Top())
}

object StreamCtrl extends App {
  val n = 8
  case class D() extends Bundle {
    val x = UInt(n bits)
    // val y = UInt(n bits)
  }
  case class Top() extends Component {
    val i = slave Stream(D()) // Stream adds valid and ready to a data
    val o = master Stream(D())

    val x1 = Reg(D())
    val x1Valid = Reg(Bool())
    val x1Ready = Bool()
    x1Ready := o.ready
    i.ready := x1Ready
    x1Valid := i.valid

    // if the downstream is ready or the current value is not valid, process the next data
    when(x1Ready | !x1Valid){ 
      x1.x := i.x + 1
    }

    o.x := x1.x
    o.valid := x1Valid
  }

  SimConfig.compile{
    val dut = Top()
    dut.x1.simPublic()
    dut.x1Ready.simPublic()
    dut.x1Valid.simPublic()
    dut
  }.doSim{ dut =>
    val cd = dut.clockDomain
    cd.forkStimulus(10)
    dut.i.valid #= false
    dut.o.ready #= false
    cd.assertReset()
    cd.waitSampling()
    cd.deassertReset()
    cd.waitSampling()

    println(s"x1:${dut.x1.x.toInt},valid:${dut.x1Valid.toBoolean},ready:${dut.x1Ready.toBoolean}")
    dut.i.x #= 5
    cd.waitSampling()
    sleep(1)
    println(s"x1:${dut.x1.x.toInt},valid:${dut.x1Valid.toBoolean},ready:${dut.x1Ready.toBoolean}")

    dut.i.valid #= true
    cd.waitSampling()
    sleep(1)
    println(s"x1:${dut.x1.x.toInt},valid:${dut.x1Valid.toBoolean},ready:${dut.x1Ready.toBoolean}")

    dut.i.x #= 7
    cd.waitSampling()
    sleep(1)
    println(s"x1:${dut.x1.x.toInt},valid:${dut.x1Valid.toBoolean},ready:${dut.x1Ready.toBoolean}")

    dut.o.ready #= true
    cd.waitSampling()
    sleep(1)
    println(s"x1:${dut.x1.x.toInt},valid:${dut.x1Valid.toBoolean},ready:${dut.x1Ready.toBoolean}")
  }
}
