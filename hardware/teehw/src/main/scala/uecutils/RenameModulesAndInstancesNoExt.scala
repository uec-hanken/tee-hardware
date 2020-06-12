// See LICENSE for license details.

package uec.teehardware.uecutils

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
import wiring._

// TODO: Not used yet
class ReplaceMemMacrosNotExt(writer: ConfWriter, name: String) extends ReplaceMemMacros(writer) {
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
          val newMemBBName = namespace newName s"${newWrapperName}_ext_in${name}"
          val newMem = m copy (name = newMemBBName)
          memMods ++= createMemModule(newMem, newWrapperName)
          WDefInstance(m.info, m.name, newWrapperName, UnknownType)
        case Some((module, mem)) =>
          WDefInstance(m.info, m.name, nameMap(module -> mem), UnknownType)
      }
    case sx => sx map updateMemStmts(namespace, nameMap, mname, memPortMap, memMods)
  }
}

class ReplSeqMemNotExt(name: String) extends ReplSeqMem {
  override def transforms(inConfigFile: Option[YamlFileReader], outConfigFile: ConfWriter): Seq[Transform] =
    Seq(new SimpleMidTransform(Legalize),
      new SimpleMidTransform(ToMemIR),
      new SimpleMidTransform(ResolveMaskGranularity),
      new SimpleMidTransform(RenameAnnotatedMemoryPorts),
      new ResolveMemoryReference,
      new CreateMemoryAnnotations(inConfigFile),
      new ReplaceMemMacrosNotExt(outConfigFile, name),
      new WiringTransform,
      new SimpleMidTransform(RemoveEmpty),
      new SimpleMidTransform(CheckInitialization),
      new SimpleMidTransform(InferTypes),
      Uniquify,
      new SimpleMidTransform(ResolveKinds),
      new SimpleMidTransform(ResolveFlows))
}
