package uec.teehardware.uecutils

import firrtl._
import firrtl.ir._
import firrtl.annotations._
import firrtl.stage.FirrtlCircuitAnnotation
import firrtl.passes.Pass
import java.io.File

import firrtl.annotations.AnnotationYamlProtocol._
import firrtl.passes.memlib.{ReplSeqMem, ReplSeqMemAnnotation}
import firrtl.transforms.BlackBoxResourceFileNameAnno
import net.jcazevedo.moultingyaml._
import logger.LazyLogging
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
  def topTransforms: Seq[Transform] = {
    Seq(
      new ReParentCircuit,
      new RemoveUnusedModules,
      new AvoidExtModuleCollisions,
      new AddSuffixToModuleNames
    )
  }

  lazy val rootCircuitTarget = CircuitTarget(harnessTop.get)

  def topAnnos(name: String, topExtModules: Seq[ExtModule]) =
    Seq(ReParentCircuitAnnotation(rootCircuitTarget.module(name))) ++
      Seq(BlackBoxResourceFileNameAnno(targetDir + name + ".f"))  ++
      Seq(KeepNameAnnotation(rootCircuitTarget.module(name))) ++
      Seq(ModuleNameSuffixAnnotation(rootCircuitTarget, s"_in${name}")) ++
      Seq(LinkExtModulesAnnotation(topExtModules))

  def topOptions(name: String, topExtModules: Seq[ExtModule]) = firrtlOptions.copy(
    customTransforms = firrtlOptions.customTransforms.map{
      case _: ReplSeqMem => new ReplSeqMemNotExt
      case other => other
    } ++ topTransforms,
    annotations = firrtlOptions.annotations.map({
      case ReplSeqMemAnnotation(i, o) => ReplSeqMemAnnotation(i, targetDir + name + ".mems.conf")
      case a => a
    }) ++ topAnnos(name, topExtModules),
    outputFileNameOverride = targetDir + name + ".v"
  )

  private def chipTransforms: Seq[Transform] = {
    Seq(
      new ReParentCircuit,
      new ConvertToExtMod,
      new RemoveUnusedModules,
      new AvoidExtModuleCollisions,
      new AddSuffixToModuleNames
    )
  }

  def harnessTransforms: Seq[Transform] = {
    Seq(
      new ReParentCircuit,
      new ConvertToExtMod,
      new RemoveUnusedModules,
      new AvoidExtModuleCollisions,
      new AddSuffixToModuleNames
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
        case da: DeletedAnnotation => false
        case ec: EmittedComponent => false
        case ea: EmittedAnnotation[_] => false
        case fca: FirrtlCircuitAnnotation => false
        case _ => true
      })))
      outputFile.close()
    }
  }

  // TopGeneration
  protected def executeTop(name: String, topExtModules: Seq[ExtModule]): Seq[ExtModule] = {
    println("Attempting to extract " + name + "...")
    optionsManager.firrtlOptions = topOptions(name, topExtModules)
    try {
      val result = firrtl.Driver.execute(optionsManager)
      result match {
        case x: FirrtlExecutionSuccess =>
          dump(x, Some(targetDir + name + ".fir"), Some(targetDir + name + ".anno.json"))
          x.circuitState.circuit.modules.collect { case e: ExtModule => e }
        case _ =>
          println("Failed to extract " + name + ", continuing...")
          //throw new Exception("executeTop failed on illegal FIRRTL Chip Top!")
          Seq()
      }
    }
    catch {
      case a : Throwable =>
        println("Failed to extract " + name + ", continuing...")
        //throw a
        Seq()
    }
  }

  // Multi Top Generation
  protected def executeMultiTop(): Seq[ExtModule] = {
    var currentExt : Seq[ExtModule] = Seq()
    synTops.foreach {
      case name =>
        val ext = executeTop(name, currentExt)
        currentExt = currentExt ++ ext
        //println("Ext: " + ext.map(_.name).mkString(" "))
        //println("CurrentExt: " + currentExt.map(_.name).mkString(" "))
    }
    currentExt
  }

  // Top and Chip generation
  protected def executeMultiTopAndChip(): Seq[ExtModule] = {
    // Execute top and get list of ExtModules to avoid collisions
    val topExtModules = executeMultiTop()
    //val topExtModules = Seq[ExtModule]()
    
    val externals = chipTop ++ synTops
    
    val chipAnnos =
      Seq(ReParentCircuitAnnotation(rootCircuitTarget.module(chipTop.get))) ++
      Seq(BlackBoxResourceFileNameAnno(targetDir + chipTop.get + ".f")) ++
      externals.map(ext => KeepNameAnnotation(rootCircuitTarget.module(ext))) ++
      chipTop.map(ht => ModuleNameSuffixAnnotation(rootCircuitTarget, s"_in${ht}")) ++
      synTops.map(st => ConvertToExtModAnnotation(rootCircuitTarget.module(st))) :+
      LinkExtModulesAnnotation(topExtModules)

    println("Attempting to extract " + chipTop.get + "...")

    // For chip run, change some firrtlOptions (below) for harness phase
    // customTransforms: setup harness transforms, add AvoidExtModuleCollisions
    // outputFileNameOverride: change to harnessOutput
    // conf file must change to harnessConf by mapping annotations
    optionsManager.firrtlOptions = firrtlOptions.copy(
      customTransforms = firrtlOptions.customTransforms ++ chipTransforms,
      outputFileNameOverride = targetDir + chipTop.get + ".v",
      annotations = firrtlOptions.annotations.map({
        case ReplSeqMemAnnotation(i, o) => ReplSeqMemAnnotation(i, targetDir + chipTop.get + ".mems.conf")
        case a => a
      }) ++ chipAnnos
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
        throw new Exception("executeMultiTopAndChip failed on illegal FIRRTL Chip Top!")
    }
  }

  // Chip and Harness generation
  protected def executeChipAndHarness(): Unit = {
    // Execute top and get list of ExtModules to avoid collisions
    val topExtModules = executeMultiTopAndChip()
    
    val externals = Seq("SimSerial", "SimDTM") ++ harnessTop ++ chipTop

    println("Attempting to extract " + multiTopOptions.harnessTop.get + "...")

    val harnessAnnos =
      Seq(BlackBoxResourceFileNameAnno(targetDir + multiTopOptions.harnessTop.get + ".f")) ++
      externals.map(ext => KeepNameAnnotation(rootCircuitTarget.module(ext))) ++
      harnessTop.map(ht => ModuleNameSuffixAnnotation(rootCircuitTarget, s"_in${ht}")) ++
      Seq(ConvertToExtModAnnotation(rootCircuitTarget.module(chipTop.get))) :+
      LinkExtModulesAnnotation(topExtModules)

    // For harness run, change some firrtlOptions (below) for harness phase
    // customTransforms: setup harness transforms, add AvoidExtModuleCollisions
    // outputFileNameOverride: change to harnessOutput
    // conf file must change to harnessConf by mapping annotations
    optionsManager.firrtlOptions = firrtlOptions.copy(
      customTransforms = firrtlOptions.customTransforms ++ harnessTransforms,
      outputFileNameOverride = targetDir + harnessTop.get + ".v",
      annotations = firrtlOptions.annotations.map({
        case ReplSeqMemAnnotation(i, o) => ReplSeqMemAnnotation(i, targetDir + harnessTop.get + ".mems.conf")
        case a => a
      }) ++ harnessAnnos
    )
    val harnessResult = firrtl.Driver.execute(optionsManager)
    harnessResult match {
      case x: FirrtlExecutionSuccess =>
        dump(
          x,
          Some(targetDir + multiTopOptions.harnessTop.get + ".fir"),
          Some(targetDir + multiTopOptions.harnessTop.get + ".anno.json"))
      case x =>
        throw new Exception(s"executeHarness failed while executing FIRRTL!\n${x}")
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
