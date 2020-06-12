// See LICENSE for license details.

package uec.teehardware.uecutils

import java.io.File

import firrtl._
import firrtl.annotations._
import firrtl.ir._
import firrtl.passes.MemPortUtils._
import firrtl.passes._
import firrtl.passes.memlib._
import firrtl.passes.wiring.WiringTransform
import firrtl.Utils._
import firrtl.Mappers._
import MemPortUtils.{MemPortMap, Modules}
import MemTransformUtils._
import barstools.tapeout.transforms.ModuleNameSuffixAnnotation
import wiring._

class ReplaceMemMacrosNotExt(writer: ConfWriter, suffix: String) extends ReplaceMemMacros(writer) {
  private type NameMap = collection.mutable.HashMap[(String, String), String]
  override def updateMemStmts(namespace: Namespace,
                              nameMap: NameMap,
                              mname: String,
                              memPortMap: MemPortMap,
                              memMods: Modules)
                             (s: Statement): Statement = s match {
    case m: DefAnnotatedMemory =>
      if (m.maskGran.isEmpty) {
        m.writers foreach { w => memPortMap(s"${m.name}.$w.mask") = EmptyExpression }
        m.readwriters foreach { w => memPortMap(s"${m.name}.$w.wmask") = EmptyExpression }
      }
      m.memRef match {
        case None =>
          // prototype mem
          val newWrapperName = nameMap(mname -> m.name)
          val newMemBBName = namespace newName s"${newWrapperName}_ext${suffix}"
          val newMem = m copy (name = newMemBBName)
          memMods ++= createMemModule(newMem, newWrapperName)
          WDefInstance(m.info, m.name, newWrapperName, UnknownType)
        case Some((module, mem)) =>
          WDefInstance(m.info, m.name, nameMap(module -> mem), UnknownType)
      }
    case sx => sx map updateMemStmts(namespace, nameMap, mname, memPortMap, memMods)
  }
}

class ReplSeqMemNotExt extends ReplSeqMem {
  def other_transforms(inConfigFile: Option[YamlFileReader], outConfigFile: ConfWriter, suffix: String): Seq[Transform] =
    Seq(new SimpleMidTransform(Legalize),
      new SimpleMidTransform(ToMemIR),
      new SimpleMidTransform(ResolveMaskGranularity),
      new SimpleMidTransform(RenameAnnotatedMemoryPorts),
      new ResolveMemoryReference,
      new CreateMemoryAnnotations(inConfigFile),
      new ReplaceMemMacrosNotExt(outConfigFile, suffix),
      new WiringTransform,
      new SimpleMidTransform(RemoveEmpty),
      new SimpleMidTransform(CheckInitialization),
      new SimpleMidTransform(InferTypes),
      Uniquify,
      new SimpleMidTransform(ResolveKinds),
      new SimpleMidTransform(ResolveFlows))
  override def execute(state: CircuitState): CircuitState = {
    // Lets use their good'old ModuleNameSuffixAnnotation
    val suffixes = state.annotations.collect { case ModuleNameSuffixAnnotation(_, suffix) => suffix }
    require(suffixes.length <= 1)

    val suffix = suffixes.headOption.getOrElse("")

    // The rest is identical (I hate you, firrtl devs)
    // But we change the transform to include the name
    val annos = state.annotations.collect { case a: ReplSeqMemAnnotation => a }
    annos match {
      case Nil => state // Do nothing if there are no annotations
      case Seq(ReplSeqMemAnnotation(inputFileName, outputConfig)) =>
        val inConfigFile = {
          if (inputFileName.isEmpty) None
          else if (new File(inputFileName).exists) Some(new YamlFileReader(inputFileName))
          else error("Input configuration file does not exist!")
        }
        val outConfigFile = new ConfWriter(outputConfig)
        other_transforms(inConfigFile, outConfigFile, suffix).foldLeft(state) { (in, xform) => xform.runTransform(in) }
      case _ => error("Unexpected transform annotation")
    }
  }
}
