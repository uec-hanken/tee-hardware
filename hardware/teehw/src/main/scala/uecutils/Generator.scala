package uec.teehardware.uecutils

import firrtl._
import firrtl.ir._
import firrtl.annotations._
import firrtl.stage.FirrtlCircuitAnnotation
import firrtl.passes.Pass

import java.io.File
import firrtl.annotations.AnnotationYamlProtocol._
import firrtl.passes.memlib.ReplSeqMemAnnotation
import firrtl.transforms.BlackBoxResourceFileNameAnno
import net.jcazevedo.moultingyaml._
import com.typesafe.scalalogging.LazyLogging
import barstools.tapeout.transforms._

sealed trait MultiTopApp extends LazyLogging { this: App =>
  lazy val optionsManager = {
    val optionsManager = new ExecutionOptionsManager("tapeout") with HasFirrtlOptions with HasMultiTopOptions
    if (!optionsManager.parse(args)) {
      throw new Exception("Error parsing options!")
    }
    optionsManager
  }
  lazy val multiTopOptions = optionsManager.multiTopOptions
  // Tapeout options
  lazy val synTops = multiTopOptions.synTops
  lazy val chipTop = multiTopOptions.chipTop
  lazy val harnessTop = multiTopOptions.harnessTop
  lazy val firrtlOptions = optionsManager.firrtlOptions
  // FIRRTL options
  lazy val annoFiles = firrtlOptions.annotationFileNames
  lazy val targetDir = optionsManager.commonOptions.targetDirName + "/"

  // This gets the transforms for a specific top
  def topTransforms(name: String): Seq[Transform] = {
    Seq(
      new ReParentCircuit(name),
      new RemoveUnusedModules
    )
  }

  def topOptions(name: String) = firrtlOptions.copy(
    customTransforms = firrtlOptions.customTransforms ++ topTransforms(name),
    annotations = firrtlOptions.annotations.map({
      case ReplSeqMemAnnotation(i, o) => ReplSeqMemAnnotation(i, targetDir + name + ".mems.conf")
      case a => a
    }) ++ List(BlackBoxResourceFileNameAnno(targetDir + name + ".f")),
    outputFileNameOverride = targetDir + name + ".v"
  )

  class AvoidExtModuleCollisions(mustLink: Seq[ExtModule]) extends Transform {
    def inputForm = HighForm
    def outputForm = HighForm
    def execute(state: CircuitState): CircuitState = {
      state.copy(circuit = state.circuit.copy(modules = state.circuit.modules ++ mustLink))
    }
  }

  private def chipTransforms(topExtModules: Seq[ExtModule]): Seq[Transform] = {
    val externals = Set(chipTop.get) ++ synTops
    Seq(
      new ReParentCircuit(chipTop.get),
      new ConvertToExtMod((m) => synTops.contains(m.name)),
      new RemoveUnusedModules,
      new AvoidExtModuleCollisions(topExtModules),
      new RenameModulesAndInstances((old) => if (externals contains old) old else (old + "_in" + chipTop.get))
    )
  }

  private def harnessTransforms(topExtModules: Seq[ExtModule]): Seq[Transform] = {
    // XXX this is a hack, we really should be checking the masters to see if they are ExtModules
    val externals = Set(harnessTop.get, chipTop.get, "SimSerial", "SimDTM")
    Seq(
      new ReParentCircuit(harnessTop.get),
      new ConvertToExtMod((m) => m.name == multiTopOptions.chipTop.get),
      new RemoveUnusedModules,
      new AvoidExtModuleCollisions(topExtModules),
      new RenameModulesAndInstances((old) => if (externals contains old) old else (old + "_in" + harnessTop.get))
    )
  }

  // Dump firrtl and annotation files
  protected def dump(res: FirrtlExecutionSuccess, firFile: Option[String], annoFile: Option[String]): Unit = {
    firFile.foreach { firPath =>
      val outputFile = new java.io.PrintWriter(firPath)
      outputFile.write(res.circuitState.circuit.serialize)
      outputFile.close()
    }
    annoFile.foreach { annoPath =>
      val outputFile = new java.io.PrintWriter(annoPath)
      outputFile.write(JsonProtocol.serialize(res.circuitState.annotations.filter(_ match {
        case ea: EmittedAnnotation[_] => false
        case fca: FirrtlCircuitAnnotation => false
        case _ => true
      })))
      outputFile.close()
    }
  }

  // TopGeneration
  protected def executeTop(name: String): Seq[ExtModule] = {
    optionsManager.firrtlOptions = topOptions(name)
    val result = firrtl.Driver.execute(optionsManager)
    result match {
      case x: FirrtlExecutionSuccess =>
        dump(x, Some(targetDir + name + ".fir"), Some(targetDir + name + ".anno.json"))
        x.circuitState.circuit.modules.collect{ case e: ExtModule => e }
      case _ =>
        throw new Exception("executeTop failed on illegal FIRRTL input!")
    }
  }

  // Multi Top Generation
  protected def executeMultiTop(): Seq[ExtModule] = {
    synTops.flatMap {
      case name =>
        executeTop(name)
    }
  }

  // Top and Chip generation
  protected def executeMultiTopAndChip(): Seq[ExtModule] = {
    // Execute top and get list of ExtModules to avoid collisions
    val topExtModules = executeMultiTop()

    optionsManager.firrtlOptions = firrtlOptions.copy(
      customTransforms = firrtlOptions.customTransforms ++ chipTransforms(topExtModules),
      outputFileNameOverride = targetDir + multiTopOptions.chipTop.get + ".v",
      annotations = firrtlOptions.annotations.map({
        case ReplSeqMemAnnotation(i, o) => ReplSeqMemAnnotation(i, targetDir + multiTopOptions.chipTop.get + ".mems.conf")
        case a => a
      }) ++ List(BlackBoxResourceFileNameAnno(targetDir + multiTopOptions.chipTop.get + ".f"))
    )
    val chipResult = firrtl.Driver.execute(optionsManager)
    chipResult match {
      case x: FirrtlExecutionSuccess =>
        dump(
          x,
          Some(targetDir + multiTopOptions.chipTop.get + ".fir"),
          Some(targetDir + multiTopOptions.chipTop.get + ".anno.json"))
        x.circuitState.circuit.modules.collect{ case e: ExtModule => e }
      case _ =>
        throw new Exception("executeTop failed on illegal FIRRTL input!")
    }
  }

  // Chip and Harness generation
  protected def executeChipAndHarness(): Unit = {
    // Execute top and get list of ExtModules to avoid collisions
    val topExtModules = executeMultiTopAndChip()

    optionsManager.firrtlOptions = firrtlOptions.copy(
      customTransforms = firrtlOptions.customTransforms ++ harnessTransforms(topExtModules),
      outputFileNameOverride = targetDir + multiTopOptions.harnessTop.get + ".v",
      annotations = firrtlOptions.annotations.map({
        case ReplSeqMemAnnotation(i, o) => ReplSeqMemAnnotation(i, targetDir + multiTopOptions.harnessTop.get + ".mems.conf")
        case a => a
      }) ++ List(BlackBoxResourceFileNameAnno(targetDir + multiTopOptions.harnessTop.get + ".f"))
    )
    val harnessResult = firrtl.Driver.execute(optionsManager)
    harnessResult match {
      case x: FirrtlExecutionSuccess =>
        dump(
          x,
          Some(targetDir + multiTopOptions.harnessTop.get + ".fir"),
          Some(targetDir + multiTopOptions.harnessTop.get + ".anno.json"))
      case _ =>
    }
  }
}

object MultiTop extends App with MultiTopApp {
  // Only need a single phase to generate the top module
  executeMultiTop()
}

object MultiTopAndHarness extends App with MultiTopApp {
  executeChipAndHarness()
}