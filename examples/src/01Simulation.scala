package sim

import spinal.core._
import spinal.core.sim._
import spinal.lib._

object HelloWorld extends App {
  println("Hello world")
}

object SimpleComponent extends App {
  case class Top() extends Component {
    val i = in(Bool())
    val o = out(Bool())
    o := i
  }

  SpinalVerilog{Top()}
}

// simple
object Simple extends App {
  // case class Top() extends Component {
  //   val i = in(Bool())
  //   val o = out(Bool())
  //   o := i
  // }
  // SimConfig.compile{
  //   val dut = Top()
  //   dut
  // }.doSim{

  SimConfig.compile{
    val dut = new Component {
      val i = in(Bool())
      val o = out(Bool())
      o := i
    }  
    dut
  }.doSim{ dut =>
    // in simulation, we need to assign with #=
    // dut.i := True will report error
    var b = true
    dut.i #= b
    // #= takes time 1 to effect
    for(i <- 0 to 5) {
      println(s"time: ${simTime()}, i: ${dut.i.toBoolean}, o: ${dut.o.toBoolean}")
      dut.i #= b
      b = !b
      sleep(1)
    }
  }
}

// combinational logic
object CombLogic extends App {
  SimConfig.compile{
    val dut = new Component {
      val i = in(Bool())
      val o = out(Bool())
      val m = Bool()
      m := !i
      o := !m
    }  
    dut.m.simPublic
    dut
  }.doSim{ dut =>
    dut.i #= true
    // combinational logic m := !i doesn't take time
    for(i <- 0 to 5) {
      println(s"time: ${simTime()}, i: ${dut.i.toBoolean}, m: ${dut.m.toBoolean}, o: ${dut.o.toBoolean}")
      sleep(1)
    }
  }
}

// sequential
object SeqLogicFail extends App {
  SimConfig.compile{
    val dut = new Component {
      val i = in(Bool())
      val o = out(Bool())
      val m = Reg(Bool()) init False
      m := !i
      o := !m
    }  
    dut.m.simPublic()
    dut
  }.doSim{ dut =>
    dut.i #= true
    // sequential logic (Reg) doesn't work by default
    // need to specify clock
    // see SeqLogic
    for(i <- 0 to 10) {
      println(s"time: ${simTime()}, i: ${dut.i.toBoolean}, m: ${dut.m.toBoolean}, o: ${dut.o.toBoolean}")
      sleep(1)
    }
  }
}

object SeqLogic extends App {
  SimConfig.compile{
    val dut = new Component {
      val i = in(Bool())
      val o = out(Bool())
      val m = Reg(Bool()) init False
      m := !i
      o := !m
    }  
    dut.m.simPublic()
    dut
  }.doSim{ dut =>
    val cd = dut.clockDomain
    cd.forkStimulus(10) // 1 clock cycle takes time 10
    
    // routine code start
    cd.waitSampling()
    cd.assertReset() // init the value of registers
    cd.waitRisingEdge() // wait for a rising edge of the clock so that the init can be execute
    cd.deassertReset() // end the init
    cd.waitSampling() // wait
    // routine code end

    // play with waitSampling
    println(s"start time: ${simTime()}")
    cd.waitFallingEdge()
    println(s"falling time: ${simTime()}")
    cd.waitRisingEdge()
    println(s"rising time: ${simTime()}")
    cd.waitSampling()
    println(s"another sampling: ${simTime()}")
    
    println("")
    println("check the update of Reg")
    dut.i #= true
    for(i <- 0 to 20) {
      println(s"time: ${simTime()}, i: ${dut.i.toBoolean}, m: ${dut.m.toBoolean}, o: ${dut.o.toBoolean}")
      sleep(1)
    }
  }
}