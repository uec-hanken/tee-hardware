// See LICENSE.SiFive for license details.

// This file is just a copy of Generator.scala from rocket-chip. And this is caused by the awful makefile
// infrastructure of the original project. Is a TODO to create a pull request over the makefiles.

package uec.teehardware.exampletop

import scala.util.Try

import chisel3._
import chipyard._
import chipyard.stage._
import firrtl.options.{StageMain}

object Generator extends StageMain(new ChipyardStage)

