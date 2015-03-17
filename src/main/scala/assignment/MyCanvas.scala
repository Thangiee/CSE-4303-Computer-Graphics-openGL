package assignment

import java.awt.event.{KeyEvent, KeyListener}
import javax.media.opengl._
import javax.media.opengl.awt.GLCanvas
import javax.media.opengl.fixedfunc.GLMatrixFunc._
import javax.media.opengl.glu.GLU
import javax.swing.JFileChooser

import assignment.Projection.{Parallel, Perspective}
import com.jogamp.opengl.util.FPSAnimator
import utils._

import scala.collection.mutable.ListBuffer

class MyCanvas(width: Int, height: Int, cap: GLCapabilities) extends GLCanvas(cap) with GLEventListener with KeyListener {
  private val vertexes  = new ListBuffer[Vertex]()
  private val faces     = new ListBuffer[Face]()
  private val cameras   = new ListBuffer[Camera]()
  private var inputFile = "pyramid_05.txt"

  private var glu: GLU = _
  private var gl: GL2 = _

  setSize(width, height)
  addGLEventListener(this)
  addKeyListener(this)

  override def init(glAutoDrawable: GLAutoDrawable): Unit = {
    // parse cameras data
    io.Source.fromFile("cameras_05.txt").getLines().mkString("\n").split("\n").map { line =>
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
    gl.glDepthFunc(GL.GL_LEQUAL)
    gl.glClearColor(0f, 0f, 0f, 1f)

    val animator = new FPSAnimator(this, 60)
    animator.start()
//    gl.glMatrixMode(GL_PROJECTION)
//    gl.glLoadIdentity()
//
//    gl.glMatrixMode(GL_MODELVIEW)
//    gl.glLoadIdentity()
//
//    val glu = GLU.createGLU(gl)
//
//    val c = cameras(0)
//    println(c)
//    glu.gluLookAt(
//      c.vrp.x, c.vrp.y, c.vrp.z,
//      c.vpn.x, c.vpn.y, c.vpn.z,
//      c.vup.x, c.vup.y, c.vup.z
//    )
  }

  override def display(glAutoDrawable: GLAutoDrawable): Unit = {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)

    setCamera(gl, glu, 1000)
    update()
    render(glAutoDrawable)
  }

  override def reshape(glAutoDrawable: GLAutoDrawable, x: Int, y: Int, w: Int, h: Int): Unit = {
    val gl = glAutoDrawable.getGL
    gl.glViewport(0, 0, width, height)
  }

  override def dispose(glAutoDrawable: GLAutoDrawable): Unit = {}

  private def update(): Unit = {}

  private def render(drawable: GLAutoDrawable): Unit = {
    drawViewport(cameras(0).viewport)
//    cameras.map(c => drawViewport(c.viewport, gl))
    val w = getWidth
    val h = getHeight

    gl.glColor3d(1.0, 1.0, 0.0); // Color (RGB): Yellow
    faces.map { f =>
      val v1 = vertexes(f.k - 1)
      val v2 = vertexes(f.l - 1)
      val v3 = vertexes(f.m - 1)

      gl.glBegin(GL.GL_LINE_LOOP)
      gl.glVertex2d(v1.x, v1.y)
      gl.glVertex2d(v2.x, v2.y)
      gl.glVertex2d(v3.x, v3.y)
      gl.glEnd()
    }
  }

  private def setCamera(gl: GL2, glu: GLU, distance: Double): Unit = {
    gl.glMatrixMode(GL_PROJECTION)
    gl.glLoadIdentity()
    val ratio = getWidth.toDouble / getHeight.toDouble
    glu.gluPerspective(45, ratio, 1, 1000)
    glu.gluLookAt(
      0, 0, distance,
      0, 0, 0,
      0, 1, 0
    )

    gl.glMatrixMode(GL_MODELVIEW)
    gl.glLoadIdentity()
  }

  private def drawViewport(vp: Viewport): Unit = {
    val w = getWidth
    val h = getHeight
    println(w, h)
    gl.glColor3d(1.0, 1.0, 0.0); // Color (RGB): Yellow
    gl.glBegin(GL.GL_LINE_LOOP)
    gl.glVertex2d(w * vp.minX, h * vp.minY)
    gl.glVertex2d(w * vp.minX, h * vp.maxY)
    gl.glVertex2d(w * vp.maxX, h * vp.maxY)
    gl.glVertex2d(w * vp.maxX, h * vp.minY)
    gl.glEnd()
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
      case 's' => scale(1.05)
      case 'S' => scale(1/1.05)
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
    io.Source.fromFile(inputFile).getLines().mkString("\n").split("\n").map { line =>
      line.head match {
        case 'v' => vertexes += parseVertex(line)
        case 'f' => faces += parseFace(line)
        case _   =>
      }
    }
  }

  private def rotate(degrees: Double, axis: Char): Unit = {

  }

  private def scale(factor: Double): Unit = {

  }

  private def moveEye(direction: Char): Unit = {

  }
}
