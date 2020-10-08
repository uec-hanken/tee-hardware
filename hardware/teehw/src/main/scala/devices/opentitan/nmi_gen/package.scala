package uec.teehardware.devices.opentitan

import chisel3._
import freechips.rocketchip.diplomacy._

// I am not... entirely sure why this is defined. Is defined in the interrupts, and almost magically gets converted
// from the regular nodes, but get into handles? what?
// Please use another types, like directly the nexus or the identity
package object nmi_gen
{
  type EscInwardNode = InwardNodeHandle[EscSourcePortParameters, EscSinkPortParameters, EscEdgeIn, esc_t]
  type EscOutwardNode = OutwardNodeHandle[EscSourcePortParameters, EscSinkPortParameters, EscEdgeOut, esc_t]
  type EscNode = NodeHandle[EscSourcePortParameters, EscSinkPortParameters, EscEdge, esc_t, EscSourcePortParameters, EscSinkPortParameters, EscEdge, esc_t]

  // We do not support clock domain for escs, because they are not supposed to.
  /*implicit class IntClockDomainCrossing(val x: HasClockDomainCrossing) extends AnyVal {
    def crossIn (n: IntInwardNode) (implicit valName: ValName) = EscInwardCrossingHelper(valName.name, x, n)
    def crossOut(n: IntOutwardNode)(implicit valName: ValName) = IntOutwardCrossingHelper(valName.name, x, n)
    def cross(n: IntInwardNode) (implicit valName: ValName) = crossIn(n)
    def cross(n: IntOutwardNode)(implicit valName: ValName) = crossOut(n)
  }*/
}
