package pipeline

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline

// Simple implemented without Pipeline Api
object Simple extends App {
  val n = 8
  case class Stage1() extends Component {
    val a = in(UInt(n bits))
    val b = in(UInt(n bits))
    val c = in(UInt(n bits))
    val aMulB = out(UInt(n bits))
    val cOut = out(UInt(n bits))

    aMulB :=(a * b).resized
    cOut := c
  }
  case class Stage2() extends Component {
    val aMulB = in(UInt(n bits))
    val c = in(UInt(n bits))
    val aMulBAddC = out(UInt(n bits))

    aMulBAddC := aMulB + c
  }
  case class Top() extends Component {
    val a = in(UInt(n bits))
    val b = in(UInt(n bits))
    val c = in(UInt(n bits))
    val aMulBAddC = out(UInt(n bits))

    val s1 = Stage1()
    val s2 = Stage2()
    
    s1.a := a
    s1.b := b
    s1.c := c

    val s1Res= new Bundle {
      val aMulB = Reg(UInt(n bits))
      val c = Reg(UInt(n bits))
    }

    s1Res.aMulB := s1.aMulB
    s1Res.c := s1.cOut

    s2.aMulB := s1Res.aMulB
    s2.c := s1Res.c

    aMulBAddC := s2.aMulBAddC
  }

  SimConfig.compile{
    val dut = Top()
    dut
  }.doSim{ dut =>
    val cd = dut.clockDomain
    cd.forkStimulus(10)

    dut.a #= 1
    dut.b #= 2
    dut.c #= 3

    for(i <- 0 to 3) {
      println(s"time: ${simTime()}, aMulBAddC: ${dut.aMulBAddC.toInt}")
      cd.waitSampling()
    }

  }
}

// Simple implemented with Pipeline Api
// https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Libraries/Pipeline/introduction.html
object SimplePipelineApi extends App {
  val n = 8
  case class Top() extends Component {
    val a = in(UInt(n bits))
    val b = in(UInt(n bits))
    val c = in(UInt(n bits))
    val aMulBAddC = out(UInt(n bits))

    val stage1, stage2 = pipeline.Node()
    val link = pipeline.StageLink(stage1, stage2)

    val AMULB = pipeline.Payload(UInt(n bits))
    val C = pipeline.Payload(UInt(n bits))

    stage1(AMULB) := (a * b).resized
    stage1(C) := c

    aMulBAddC := stage2(AMULB) + stage2(C)

    pipeline.Builder(link)
  }

  SimConfig.compile{
    val dut = Top()
    dut.stage1(dut.AMULB).simPublic
    dut
  }.doSim{ dut =>
    val cd = dut.clockDomain
    cd.forkStimulus(10)

    dut.a #= 1
    dut.b #= 2
    dut.c #= 3

    for(i <- 0 to 3) {
      println(s"time: ${simTime()}, aMulB: ${dut.stage1(dut.AMULB).toInt}, aMulBAddC: ${dut.aMulBAddC.toInt}")
      cd.waitSampling()
    }

  }
}