package assignment

import java.awt.{Color, Font}
import java.awt.event.{KeyEvent, KeyListener}
import javax.media.opengl._
import javax.media.opengl.awt.GLCanvas
import javax.media.opengl.fixedfunc.GLMatrixFunc._
import javax.media.opengl.glu.GLU
import javax.swing.JFileChooser

import assignment.Projection.{Parallel, Perspective}
import com.jogamp.opengl.util.FPSAnimator
import com.jogamp.opengl.util.awt.TextRenderer
import utils._

import scala.collection.mutable.ListBuffer

class MyCanvas(width: Int, height: Int, cap: GLCapabilities) extends GLCanvas(cap) with GLEventListener with KeyListener {
  private val vertexes  = new ListBuffer[Vertex]()
  private val faces     = new ListBuffer[Face]()
  private val cameras   = new ListBuffer[Camera]()
  private val textRenderer = new TextRenderer(new Font("Verdana", Font.BOLD, 12))
  private var inputFile = "pyramid_05.txt"

  private var glu: GLU = _
  private var gl: GL2 = _

  private var angleX = 0.0
  private var angleY = 0.0
  private var angleZ = 0.0
  private var transX = 0.0
  private var transY = 0.0
  private var transZ = 0.0
  private var scale  = 1.0

  setSize(width, height)
  setLocation(100, 100)
  addGLEventListener(this)
  addKeyListener(this)

  override def init(glAutoDrawable: GLAutoDrawable): Unit = {
    // parse cameras data
    io.Source.fromFile("cameras_05.txt").getLines().mkString("\n").split("\n").foreach { line =>
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
    gl.glClearColor(1, 1, 0, 0)
    gl.glEnable(GL.GL_DEPTH_TEST)
    gl.glEnable(GL.GL_SCISSOR_TEST)
    gl.glDepthFunc(GL.GL_LEQUAL)
    gl.glClearColor(0f, 0f, 0f, 1f)
    val animator = new FPSAnimator(this, 60)
    animator.start()
  }

  override def display(glAutoDrawable: GLAutoDrawable): Unit = {
    render()
  }

  override def reshape(glAutoDrawable: GLAutoDrawable, x: Int, y: Int, w: Int, h: Int): Unit = {}

  override def dispose(glAutoDrawable: GLAutoDrawable): Unit = {}

  private def render(): Unit = {
    val w = getWidth
    val h = getHeight

    cameras.foreach { c =>
      val vp = c.viewport
      val vv = c.viewVolume

      gl.glScissor((vp.minX * w).toInt, (vp.minY * h).toInt, ((vp.maxX - vp.minX)* w).toInt, ((vp.maxY - vp.minY) * h).toInt)
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
      gl.glClearColor(.4f, .4f, .6f, 0)

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
      gl.glViewport((vp.minX * w).toInt, (vp.minY * h).toInt, ((vp.maxX - vp.minX)* w).toInt, ((vp.maxY - vp.minY) * h).toInt)

      // draw camera name
      textRenderer.beginRendering(200, 150)
      textRenderer.setColor(Color.YELLOW)
      textRenderer.setSmoothing(true)
      textRenderer.draw(c.name, 2, 2)
      textRenderer.endRendering()

      gl.glPushMatrix()
      update()
      gl.glColor3d(1.0, 1.0, 0.0); // Color (RGB): Yellow
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
      gl.glPopMatrix()
    }

    gl.glFlush()
  }

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
      case 'p' =>
      case _   =>
    }
  }

  override def keyPressed(e: KeyEvent): Unit = {
    e.getKeyCode match {
      case KeyEvent.VK_LEFT   => moveEye('l')
      case KeyEvent.VK_RIGHT  => moveEye('r')
      case KeyEvent.VK_UP     => moveEye('u')
      case KeyEvent.VK_DOWN   => moveEye('d')
      case _ =>
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

  private def reset(): Unit = {
    Seq(vertexes, faces).foreach(_.clear())
    angleX = 0.0
    angleY = 0.0
    angleZ = 0.0
    transX = 0.0
    transY = 0.0
    transZ = 0.0
    scale  = 1.0
  }
}
