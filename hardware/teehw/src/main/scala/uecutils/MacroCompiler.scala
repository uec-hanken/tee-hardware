package uec.teehardware.uecutils

import firrtl._
import firrtl.ir._
import firrtl.annotations._
import firrtl.stage.FirrtlCircuitAnnotation
import firrtl.passes.Pass
import java.io.File

object MacroCompiler extends App {
  try {
    barstools.macros.MacroCompiler.run(args.toList)
  }
  catch {
    case _: java.io.FileNotFoundException =>
      println("File not found. Omitting...")
    case _ : Throwable =>
      println("Unhandled exception occurred. Despite this, continuing...")
  }
}
