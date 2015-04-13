// Le, Thang
// 1000-787-155
// 2015-02-08
// Assignment_01

package utils

import assignment._


trait Parser {

  def parseVertex(text: String): Vertex = {
    val Array(x, y, z) = text.split("\\s+").tail
    Vertex(x.toDouble, y.toDouble, z.toDouble)
  }

  def parseFace(text: String): Face = {
    val Array(k, l, m, _*) = text.split("\\s+").tail
    Face(k.toInt, l.toInt, m.toInt)
  }

  def parseWindow(text: String): Window = {
    val Array(minX, minY, maxX, maxY) = text.split("\\s+").tail
    Window(minX.toDouble, minY.toDouble, maxX.toDouble, maxY.toDouble)
  }

  def parseViewVolume(text: String): ViewVolume = {
    val Array(minU, maxU, minV, maxV, minN, maxN) = text.split("\\s+").tail
    ViewVolume(minU.toDouble, maxU.toDouble, minV.toDouble, maxV.toDouble, minN.toDouble, maxN.toDouble)
  }

  def parseViewport(text: String): Viewport = {
    val Array(minX, minY, maxX, maxY) = text.split("\\s+").tail
    Viewport(minX.toDouble, minY.toDouble, maxX.toDouble, maxY.toDouble)
  }

  def parseVRP(text: String): VRP = {
    val Array(x, y, z) = text.split("\\s+").tail
    VRP(x.toDouble, y.toDouble ,z.toDouble)
  }

  def parseVPN(text: String): VPN = {
    val Array(x, y, z) = text.split("\\s+").tail
    VPN(x.toDouble, y.toDouble ,z.toDouble)
  }

  def parseVUP(text: String): VUP = {
    val Array(x, y, z) = text.split("\\s+").tail
    VUP(x.toDouble, y.toDouble ,z.toDouble)
  }

  def parsePRP(text: String): PRP = {
    val Array(x, y, z) = text.split("\\s+").tail
    PRP(x.toDouble, y.toDouble ,z.toDouble)
  }

  def parseControlPoint(text: String): CtrlPt = {
    val Array(x, y, z) = text.split("\\s+").tail
    CtrlPt(x.toDouble, y.toDouble ,z.toDouble)
  }

  def parseInt(text: String): Int = text.split("\\s+").last.toInt
}

object Parser extends Parser
