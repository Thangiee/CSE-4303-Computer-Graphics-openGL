package assignment

trait Vectorable {
  def x: Double
  def y: Double
  def z: Double

  def magnitude: Double = math.sqrt(x*x + y*y + z*z)
}

case class VRP(x: Double, y: Double, z: Double) extends Vectorable

case class VPN(x: Double, y: Double, z: Double) extends Vectorable

case class VUP(x: Double, y: Double, z: Double) extends Vectorable

case class PRP(x: Double, y: Double, z: Double) extends Vectorable

case class Vertex(x: Double, y: Double, z: Double) extends Vectorable

case class GeoVector(x: Double, y: Double, z: Double) extends Vectorable