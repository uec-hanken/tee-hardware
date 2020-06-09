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

// This doesn't rename ExtModules under the assumption that they're some
// Verilog black box and therefore can't be renamed.  Since the point is to
// allow FIRRTL to be linked together using "cat" and ExtModules don't get
// emitted, this should be safe.
class RenameModulesAndInstancesNotExt(rename: (String) => String) extends Transform {
  def inputForm = LowForm
  def outputForm = LowForm

  def renameInstances(body: Statement): Statement = {
    body match {
      case m: DefInstance => new DefInstance(m.info, m.name, rename(m.module))
      case m: WDefInstance => new WDefInstance(m.info, m.name, rename(m.module), m.tpe)
      case b: Block => new Block( b.stmts map { s => renameInstances(s) } )
      case s: Statement => s
    }
  }

  def run(state: CircuitState): (Circuit, RenameMap) = {
    val myRenames = RenameMap()
    val c = state.circuit
    val modulesx = c.modules.map {
      case m: ExtModule =>
        myRenames.record(ModuleTarget(c.main, m.name), ModuleTarget(c.main, rename(m.name)))
        m.copy(name = rename(m.name))
        //new ExtModule(m.info, rename(m.name), m.ports, rename(m.defname), m.params)
      case m: Module =>
        myRenames.record(ModuleTarget(c.main, m.name), ModuleTarget(c.main, rename(m.name)))
        new Module(m.info, rename(m.name), m.ports, renameInstances(m.body))
    }
    (Circuit(c.info, modulesx, c.main), myRenames)
  }

  def execute(state: CircuitState): CircuitState = {
    val (ret, renames) = run(state)
    state.copy(circuit = ret, renames = Some(renames))
  }
}

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
