package assignment

import java.awt.{Color, Font}
import java.awt.event.{KeyEvent, KeyListener}
import java.nio.{DoubleBuffer, FloatBuffer}
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

import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class MyCanvas(cap: GLCapabilities) extends GLCanvas(cap) with GLEventListener with KeyListener {
  private val vertexes      = new ListBuffer[Vertex]()
  private val faces         = new ListBuffer[Face]()
  private val cameras       = new ListBuffer[Camera]()
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
  setLocation(100, 100)
  addGLEventListener(this)
  addKeyListener(this)

  val ctrlPoints = ArrayBuffer[Double](
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
    gl.glEnable(GL.GL_DEPTH_TEST)
    gl.glEnable(GL.GL_SCISSOR_TEST)
    gl.glDepthFunc(GL.GL_LEQUAL)
    gl.glEnable(GL2.GL_MAP2_VERTEX_3)
    gl.glMap2d(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, DoubleBuffer.wrap(ctrlPoints.toArray))

    val animator = new FPSAnimator(this, 30)
    animator.start()
  }

  override def display(glAutoDrawable: GLAutoDrawable): Unit = {
    val w = getWidth
    val h = getHeight

    cameras.foreach { c =>
      val vp = c.viewport
      val vv = c.viewVolume

      gl.glScissor((vp.minX * w).toInt, (vp.minY * h).toInt, ((vp.maxX - vp.minX) * w).toInt, ((vp.maxY - vp.minY) * h).toInt)
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
      gl.glClearColor(0.2f, 0.2f, 0.2f, 0)
      gl.glMatrixMode(GL_PROJECTION)
      gl.glLoadIdentity()

      if (c.projType == Projection.Perspective) {
        gl.glFrustum(vv.minU, vv.maxU, vv.minV, vv.maxV, vv.minN, vv.maxN)
      } else {
        gl.glOrtho(vv.minU, vv.maxU, vv.minV, vv.maxV, vv.minN, vv.maxN)
      }

      glu.gluLookAt(
        c.vrp.x, c.vrp.y, c.vrp.z,
        c.vpn.x, c.vpn.y, c.vpn.z,
        c.vup.x, c.vup.y, c.vup.z
      )

      gl.glMatrixMode(GL_MODELVIEW)
      gl.glLoadIdentity()

      gl.glViewport((vp.minX * w).toInt, (vp.minY * h).toInt, ((vp.maxX - vp.minX) * w).toInt, ((vp.maxY - vp.minY) * h).toInt)

      // draw camera name
      textRenderer.beginRendering(200, 150)
      textRenderer.setColor(Color.YELLOW)
      textRenderer.setSmoothing(true)
      textRenderer.draw(c.name, 2, 2)
      textRenderer.endRendering()

      gl.glPushMatrix()
      update()
      render()
      gl.glPopMatrix()
      gl.glFlush()
    }
  }

  override def reshape(glAutoDrawable: GLAutoDrawable, x: Int, y: Int, w: Int, h: Int): Unit = {}

  override def dispose(glAutoDrawable: GLAutoDrawable): Unit = {}

  private def update(): Unit = {
    gl.glRotated(angleZ, 0, 0, 1)
    gl.glRotated(angleY, 0, 1, 0)
    gl.glRotated(angleX, 1, 0, 0)
    gl.glTranslated(transX, transY, transZ)
    gl.glScaled(scale, scale, scale)
  }

  private def render(): Unit = {
    gl.glColor3f(1, 0, 0)

    if (true) {
      // draw patches
      for ( j ← 0 to resolution ) {
        gl.glBegin(GL.GL_LINE_STRIP)
        for ( i ← 0 to resolution ) {
          gl.glEvalCoord2f(i / resolution.toFloat, j / resolution.toFloat)
        }
        gl.glEnd()

        gl.glBegin(GL.GL_LINE_STRIP)
        for ( i ← 0 to resolution ) {
          gl.glEvalCoord2f(j / resolution.toFloat, i / resolution.toFloat)
        }
        gl.glEnd()

        gl.glBegin(GL.GL_LINE_STRIP)
        for ( i ← 0 to (resolution - j) ) {
          gl.glEvalCoord2f(i / resolution.toFloat, (i + j) / resolution.toFloat)
        }
        gl.glEnd()

        gl.glBegin(GL.GL_LINE_STRIP)
        for ( i ← 0 to (resolution - j) ) {
          gl.glEvalCoord2f((i + j) / resolution.toFloat, i / resolution.toFloat)
        }
        gl.glEnd()
      }
    }

    gl.glColor3d(1.0, 1.0, 0.0)
    faces.foreach { f =>
      val v1 = vertexes(f.k - 1)
      val v2 = vertexes(f.l - 1)
      val v3 = vertexes(f.m - 1)

      gl.glBegin(GL.GL_LINE_LOOP)
      gl.glVertex3d(v1.x, v1.y, v1.z)
      gl.glVertex3d(v2.x, v2.y, v2.z)
      gl.glVertex3d(v3.x, v3.y, v3.z)
      gl.glEnd()
    }
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
        case 'b' => ctrlPoints ++= parseControlPoint(line)
        case 'v' => vertexes += parseVertex(line)
        case 'f' => faces += parseFace(line)
        case _   =>
      }
    }
    println(ctrlPoints)
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

  private def changeResolution(delta: Int) = {
    if (resolution > 1 && delta < 0) {
      resolution += delta
    } else if (resolution < 100 && delta > 0) {
      resolution += delta
    }
  }

  private def reset(): Unit = {
    Seq(vertexes, faces, ctrlPoints).foreach(_.clear())
    angleX = 0.0
    angleY = 0.0
    angleZ = 0.0
    transX = 0.0
    transY = 0.0
    transZ = 0.0
    scale = 1.0
  }
}
