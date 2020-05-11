// See LICENSE.SiFive for license details.

// This file is just a copy of Generator.scala from rocket-chip. And this is caused by the awful makefile
// infrastructure of the original project. Is a TODO to create a pull request over the makefiles.

package uec.keystoneAcc.exampletop

import scala.util.Try

import chisel3._

import freechips.rocketchip.config.{Parameters}
import freechips.rocketchip.util.{GeneratorApp}
import freechips.rocketchip.system.{TestGeneration}
import chipyard._

object Generator extends GeneratorApp {
  // add unique test suites
  override def addTestSuites {
    implicit val p: Parameters = params
    TestSuiteHelper.addRocketTestSuites
    TestSuiteHelper.addBoomTestSuites
    TestSuiteHelper.addArianeTestSuites

    // if hwacha parameter exists then generate its tests
    // TODO: find a more elegant way to do this. either through
    // trying to disambiguate BuildRoCC, having a AccelParamsKey,
    // or having the Accelerator/Tile add its own tests
    import hwacha.HwachaTestSuites._
    if (Try(p(hwacha.HwachaNLanes)).getOrElse(0) > 0) {
      TestGeneration.addSuites(rv64uv.map(_("p")))
      TestGeneration.addSuites(rv64uv.map(_("vp")))
      TestGeneration.addSuite(rv64sv("p"))
      TestGeneration.addSuite(hwachaBmarks)
    }
  }

  // specify the name that the generator outputs files as
  override lazy val longName = names.topModuleProject + "." + names.topModuleClass + "." + names.configs

  // generate files
  generateFirrtl
  generateAnno
  generateROMs
  generateTestSuiteMakefrags
  generateArtefacts
}
