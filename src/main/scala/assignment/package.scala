
package object assignment {

  case class CtrlPt(x: Double, y: Double, z: Double)
  case class Face(k: Int, l: Int, m: Int)
  case class Window(minX: Double, minY: Double, maxX: Double, maxY: Double)
  case class Viewport(minX: Double, minY: Double, maxX: Double, maxY: Double)
  case class ViewVolume(minU: Double, maxU: Double, minV: Double, maxV: Double, minN: Double, maxN: Double) {
    def getCenterWindow = CenterWindow((maxU + minU) / 2.0, (maxV + minV) / 2.0)
  }

  case class CenterWindow(x: Double, y: Double)

  case class Camera(
    var name: String = "",
    var projType: Projection = Projection.Parallel,
    var vrp: VRP = VRP(0, 0, 0),
    var vpn: VPN = VPN(0, 0, 1),
    var vup: VUP = VUP(0, 1, 0),
    var prp: PRP = PRP(0, 0, 1),
    var viewVolume: ViewVolume = ViewVolume(-1, 1, -1, 1, -1, 1),
    var viewport: Viewport = Viewport(.1, .1, .9, .9)
    )

  sealed abstract class Projection
  object Projection {
    case object Parallel extends Projection
    case object Perspective extends Projection
  }
}
