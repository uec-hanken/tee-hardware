package uec.teehardware.devices.opentitan

import chisel3._
import freechips.rocketchip.diplomacy._

// I am not... entirely sure why this is defined. Is defined in the interrupts, and almost magically gets converted
// from the regular nodes, but get into handles? what?
// Please use another types, like directly the nexus or the identity
package object alert
{
  type AlertInwardNode = InwardNodeHandle[AlertSourcePortParameters, AlertSinkPortParameters, AlertEdgeIn, Vec[alert_t]]
  type AlertOutwardNode = OutwardNodeHandle[AlertSourcePortParameters, AlertSinkPortParameters, AlertEdgeOut, Vec[alert_t]]
  type AlertNode = NodeHandle[AlertSourcePortParameters, AlertSinkPortParameters, AlertEdge, Vec[alert_t], AlertSourcePortParameters, AlertSinkPortParameters, AlertEdge, Vec[alert_t]]

  // We do not support clock domain for alerts, because they are not supposed to.
  /*implicit class IntClockDomainCrossing(val x: HasClockDomainCrossing) extends AnyVal {
    def crossIn (n: IntInwardNode) (implicit valName: ValName) = AlertInwardCrossingHelper(valName.name, x, n)
    def crossOut(n: IntOutwardNode)(implicit valName: ValName) = IntOutwardCrossingHelper(valName.name, x, n)
    def cross(n: IntInwardNode) (implicit valName: ValName) = crossIn(n)
    def cross(n: IntOutwardNode)(implicit valName: ValName) = crossOut(n)
  }*/
}
