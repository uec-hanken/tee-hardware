// See LICENSE.SiFive for license details.

// This file is just a copy of Generator.scala from rocket-chip. And this is caused by the awful makefile
// infrastructure of the original project. Is a TODO to create a pull request over the makefiles.

package uec.teehardware.exampletop

import freechips.rocketchip.system.{BaseConfig, DefaultTestSuites, RegressionTestSuite, TestGeneration}
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem.RocketTilesKey
import freechips.rocketchip.tile.XLen
import freechips.rocketchip.util.GeneratorApp
import utilities.TestSuiteHelper

import scala.collection.mutable.LinkedHashSet


object Generator extends GeneratorApp {
  // add unique test suites
  override def addTestSuites {
    implicit val p: Parameters = params
    TestSuiteHelper.addRocketTestSuites
    TestSuiteHelper.addBoomTestSuites
  }

  // specify the name that the generator outputs files as
  val longName = names.topModuleProject + "." + names.topModuleClass + "." + names.configs

  // generate files
  generateFirrtl
  generateAnno
  generateROMs
  generateTestSuiteMakefrags
  generateArtefacts
}

