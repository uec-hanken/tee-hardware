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
import logger.LazyLogging
import barstools.tapeout.transforms._

sealed trait GenerateTopAndHarnessApp extends LazyLogging { this: App =>
  lazy val optionsManager = {
    val optionsManager = new ExecutionOptionsManager("tapeout") with HasFirrtlOptions with HasTapeoutOptions
    if (!optionsManager.parse(args)) {
      throw new Exception("Error parsing options!")
    }
    optionsManager
  }
  lazy val tapeoutOptions = optionsManager.tapeoutOptions
  // Tapeout options
  lazy val synTop = tapeoutOptions.synTop
  lazy val harnessTop = tapeoutOptions.harnessTop
  lazy val firrtlOptions = optionsManager.firrtlOptions
  // FIRRTL options
  lazy val annoFiles = firrtlOptions.annotationFileNames

  val topTransforms = Seq(
    new ReParentCircuit,
    new RemoveUnusedModules
  )

  lazy val rootCircuitTarget = CircuitTarget(harnessTop.get)

  lazy val topAnnos = synTop.map(st => ReParentCircuitAnnotation(rootCircuitTarget.module(st))) ++
    tapeoutOptions.topDotfOut.map(BlackBoxResourceFileNameAnno(_))

  lazy val topOptions = firrtlOptions.copy(
    customTransforms = firrtlOptions.customTransforms ++ topTransforms,
    annotations = firrtlOptions.annotations ++ topAnnos
  )

  val harnessTransforms = Seq(
    new ConvertToExtMod,
    new RemoveUnusedModules,
    new AvoidExtModuleCollisions,
    new AddSuffixToModuleNames
  )

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
        case da: DeletedAnnotation => false
        case ec: EmittedComponent => false
        case ea: EmittedAnnotation[_] => false
        case fca: FirrtlCircuitAnnotation => false
        case _ => true
      })))
      outputFile.close()
    }
  }

  // Top Generation
  protected def executeTop(): Seq[ExtModule] = {
    optionsManager.firrtlOptions = topOptions
    val result = firrtl.Driver.execute(optionsManager)
    result match {
      case x: FirrtlExecutionSuccess =>
        dump(x, tapeoutOptions.topFir, tapeoutOptions.topAnnoOut)
        x.circuitState.circuit.modules.collect{ case e: ExtModule => e }
      case x =>
        throw new Exception(s"executeTop failed while executing FIRRTL!\n${x}")
    }
  }

  // Top and harness generation
  protected def executeTopAndHarness(): Unit = {
    // Execute top and get list of ExtModules to avoid collisions
    val topExtModules = executeTop()

    val externals = Seq("SimSerial", "SimDTM") ++ harnessTop ++ synTop

    val harnessAnnos =
      tapeoutOptions.harnessDotfOut.map(BlackBoxResourceFileNameAnno(_)).toSeq ++
      externals.map(ext => KeepNameAnnotation(rootCircuitTarget.module(ext))) ++
      harnessTop.map(ht => ModuleNameSuffixAnnotation(rootCircuitTarget, s"_in${ht}")) ++
      synTop.map(st => ConvertToExtModAnnotation(rootCircuitTarget.module(st))) :+
      LinkExtModulesAnnotation(topExtModules)

    // For harness run, change some firrtlOptions (below) for harness phase
    // customTransforms: setup harness transforms, add AvoidExtModuleCollisions
    // outputFileNameOverride: change to harnessOutput
    // conf file must change to harnessConf by mapping annotations
    optionsManager.firrtlOptions = firrtlOptions.copy(
      customTransforms = firrtlOptions.customTransforms ++ harnessTransforms,
      outputFileNameOverride = tapeoutOptions.harnessOutput.get,
      annotations = firrtlOptions.annotations.map({
        case ReplSeqMemAnnotation(i, o) => ReplSeqMemAnnotation(i, tapeoutOptions.harnessConf.get)
        case a => a
      }) ++ harnessAnnos
    )
    val harnessResult = firrtl.Driver.execute(optionsManager)
    harnessResult match {
      case x: FirrtlExecutionSuccess => dump(x, tapeoutOptions.harnessFir, tapeoutOptions.harnessAnnoOut)
      case x => throw new Exception(s"executeHarness failed while executing FIRRTL!\n${x}")
    }
  }
}

object GenerateTop extends App with GenerateTopAndHarnessApp {
  // Only need a single phase to generate the top module
  executeTop()
}

object GenerateTopAndHarness extends App with GenerateTopAndHarnessApp {
  executeTopAndHarness()
}
