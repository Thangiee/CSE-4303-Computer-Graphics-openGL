package assignment

import java.awt.{Color, Font}
import java.awt.event.{KeyEvent, KeyListener}
import java.nio.FloatBuffer
import javax.media.opengl._
import javax.media.opengl.awt.GLCanvas
import javax.media.opengl.fixedfunc.GLLightingFunc
import javax.media.opengl.fixedfunc.GLMatrixFunc._
import javax.media.opengl.glu.GLU
import javax.swing.JFileChooser

import assignment.Projection.{Parallel, Perspective}
import com.jogamp.opengl.util.FPSAnimator
import com.jogamp.opengl.util.awt.TextRenderer
import utils._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class MyCanvas(width: Int, height: Int, cap: GLCapabilities) extends GLCanvas(cap) with GLEventListener with KeyListener {
  private val vertexes      = new ListBuffer[Vertex]()
  private val faces         = new ListBuffer[Face]()
  private val cameras       = new ListBuffer[Camera]()
  private val controlPoints = new ListBuffer[ControlPoint]()
  private val textRenderer  = new TextRenderer(new Font("Verdana", Font.BOLD, 12))
  private var inputFile     = "patches_06.txt"
  private var resolution    = 4

  private var glu: GLU = _
  private var gl : GL2 = _

  private var angleX = 0.0
  private var angleY = 0.0
  private var angleZ = 0.0
  private var transX = 0.0
  private var transY = 0.0
  private var transZ = 0.0
  private var scale  = 1.0

  setSize(640, 480)
  //  setLocation(100, 100)
  addGLEventListener(this)
  addKeyListener(this)

  val ctrlPoints = Array(
    -1.5, -1.5, 4.0,
    -0.5, -1.5, 2.0,
    0.5, -1.5, -1.0,
    1.5, -1.5, 2.0,
    -1.5, -0.5, 1.0,
    -0.5, -0.5, 3.0,
    0.5, -0.5, 0.0,
    1.5, -0.5, -1.0,
    -1.5, 0.5, 4.0,
    -0.5, 0.5, 0.0,
    0.5, 0.5, 3.0,
    1.5, 0.5, 4.0,
    -1.5, 1.5, -2.0,
    -0.5, 1.5, -2.0,
    0.5, 1.5, 0.0,
    1.5, 1.5, -1.0
  )

  override def init(glAutoDrawable: GLAutoDrawable): Unit = {
    // parse cameras data
    io.Source.fromFile("cameras_06.txt").getLines().mkString("\n").split("\n").foreach { line =>
      line.head match {
        case 'c' => cameras += Camera()
        case 'i' => cameras.last.name = line.split(" ").last
        case 't' => cameras.last.projType = if (line.contains("parallel")) Parallel else Perspective
        case 'e' => cameras.last.vrp = parseVRP(line)
        case 'l' => cameras.last.vpn = parseVPN(line)
        case 'u' => cameras.last.vup = parseVUP(line)
        case 'w' => cameras.last.viewVolume = parseViewVolume(line)
        case 's' => cameras.last.viewport = parseViewport(line)
      }
    }

    glu = new GLU()
    gl = glAutoDrawable.getGL.getGL2

    // Global settings.
    gl.glClearColor(0, 0, 0, 0)
    gl.glMap2d(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, ctrlPoints, 0)
    gl.glEnable(GL2.GL_MAP2_VERTEX_3)
    val animator = new FPSAnimator(this, 30)
    animator.start()
  }

  override def display(glAutoDrawable: GLAutoDrawable): Unit = {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
    gl.glLoadIdentity()
    val a = 30
    val b = 30
    gl.glColor3f(1, 0, 0)
    gl.glPushMatrix()
    gl.glRotated(85.0, 1.0, 1.0, 1.0)

    gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL)
    for (j ← 0 to b) {
      gl.glBegin(GL.GL_LINE_STRIP)
      for (i ← 0 to a) {
        gl.glEvalCoord2f(i / a.toFloat, j / b.toFloat)
      }
      gl.glEnd()

      gl.glBegin(GL.GL_LINE_STRIP)
      for (i ← 0 to a) {
        gl.glEvalCoord2f(j / b.toFloat, i / a.toFloat)
      }
      gl.glEnd()

      gl.glBegin(GL.GL_LINE_STRIP)
      for (i ← 0 to (a - j)) {
        gl.glEvalCoord2f(i / b.toFloat, (i + j) / a.toFloat)
      }
      gl.glEnd()

      gl.glBegin(GL.GL_LINE_STRIP)
      for (i ← 0 to (a - j)) {
        gl.glEvalCoord2f((i + j) / b.toFloat, i / a.toFloat)
      }
      gl.glEnd()

    }

    gl.glPopMatrix()
    gl.glFlush()
  }

  override def reshape(glAutoDrawable: GLAutoDrawable, x: Int, y: Int, w: Int, h: Int): Unit = {
    gl.glViewport(0, 0, w, h)
    gl.glMatrixMode(GL_PROJECTION)
    gl.glLoadIdentity()
    if (w <= h) {
      gl.glOrtho(-5.0, 5.0, -5.0 * h / w, 5.0 * h / w, -5.0, 5.0)
    } else {
      gl.glOrtho(-5.0 * w / h, 5.0 * w / h, -5.0, 5.0, -5.0, 5.0)
    }
    gl.glMatrixMode(GL_MODELVIEW)
    gl.glLoadIdentity()
  }

  override def dispose(glAutoDrawable: GLAutoDrawable): Unit = {}


  private def update(): Unit = {
    gl.glRotated(angleX, 1, 0, 0)
    gl.glRotated(angleY, 0, 1, 0)
    gl.glRotated(angleZ, 0, 0, 1)
    gl.glTranslated(transX, transY, transZ)
    gl.glScaled(scale, scale, scale)
  }

  override def keyTyped(e: KeyEvent): Unit = {
    e.getKeyChar match {
      case 'n' => promptFilename()
      case 'd' => loadAndDisplay()
      case 'x' => rotate(5, 'x')
      case 'X' => rotate(-5, 'x')
      case 'y' => rotate(5, 'y')
      case 'Y' => rotate(-5, 'y')
      case 'z' => rotate(5, 'z')
      case 'Z' => rotate(-5, 'z')
      case 's' => scale(.05)
      case 'S' => scale(-.05)
      case 'f' => moveEye('f')
      case 'b' => moveEye('b')
      case 'r' => changeResolution(-1)
      case 'R' => changeResolution(1)
      case 'p' =>
      case _   =>
    }
  }

  override def keyPressed(e: KeyEvent): Unit = {
    e.getKeyCode match {
      case KeyEvent.VK_LEFT  => moveEye('l')
      case KeyEvent.VK_RIGHT => moveEye('r')
      case KeyEvent.VK_UP    => moveEye('u')
      case KeyEvent.VK_DOWN  => moveEye('d')
      case _                 =>
    }
  }

  override def keyReleased(e: KeyEvent): Unit = {}

  private def promptFilename(): Unit = {
    val fc = new JFileChooser("./")
    if (fc.showOpenDialog(this) == 0) inputFile = fc.getSelectedFile.getAbsolutePath
  }

  private def loadAndDisplay(): Unit = {
    println(s"load $inputFile")
    reset()
    io.Source.fromFile(inputFile).getLines().mkString("\n").split("\n").foreach { line =>
      line.head match {
        case 'n' => resolution = parseInt(line)
        case 'b' => controlPoints += parseControlPoint(line)
        case 'v' => vertexes += parseVertex(line)
        case 'f' => faces += parseFace(line)
        case _   =>
      }
    }
  }

  private def rotate(degrees: Double, axis: Char): Unit = {
    axis match {
      case 'x' | 'X' => angleX += degrees
      case 'y' | 'Y' => angleY += degrees
      case 'z' | 'Z' => angleZ += degrees
    }
  }

  private def scale(factor: Double): Unit = {
    scale += factor
  }

  private def moveEye(direction: Char): Unit = {
    direction match {
      case 'l' => transX -= .05
      case 'r' => transX += .05
      case 'd' => transY -= .05
      case 'u' => transY += .05
      case 'f' => transZ += .05
      case 'b' => transZ -= .05
    }
  }

  private def changeResolution(delta: Int) = resolution += delta

  private def reset(): Unit = {
    Seq(vertexes, faces, controlPoints).foreach(_.clear())
    angleX = 0.0
    angleY = 0.0
    angleZ = 0.0
    transX = 0.0
    transY = 0.0
    transZ = 0.0
    scale = 1.0
  }
}
