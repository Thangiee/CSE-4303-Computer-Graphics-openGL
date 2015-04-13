package assignment

import java.awt.{Color, Font}
import javax.media.opengl._
import javax.media.opengl.awt.GLCanvas
import javax.media.opengl.fixedfunc.GLMatrixFunc._
import javax.media.opengl.glu.GLU

import com.jogamp.opengl.util.FPSAnimator
import com.jogamp.opengl.util.awt.TextRenderer

class MyCanvas(vertexes: Seq[Vertex], faces: Seq[Face], cameras: Seq[Camera], ctrlPts: Seq[CtrlPt], var resolution: Int) extends GLCanvas() with GLEventListener {
  private val textRenderer = new TextRenderer(new Font("Verdana", Font.BOLD, 12))

  private var glu: GLU = _
  private var gl : GL2 = _

  private var angleX = 0.0
  private var angleY = 0.0
  private var angleZ = 0.0
  private var transX = 0.0
  private var transY = 0.0
  private var transZ = 0.0
  private var scale  = 1.0

  setLocation(100, 100)
  addGLEventListener(this)

  override def init(glAutoDrawable: GLAutoDrawable): Unit = {
    glu = new GLU()
    gl = glAutoDrawable.getGL.getGL2

    // Global settings.
    gl.glEnable(GL.GL_DEPTH_TEST)
    gl.glEnable(GL.GL_SCISSOR_TEST)
    gl.glDepthFunc(GL.GL_LEQUAL)
    gl.glEnable(GL2.GL_MAP2_VERTEX_3)

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

    ctrlPts.grouped(16).toList.foreach { patch =>  // 16 control points make 1 patch

      gl.glMap2d(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, patch.flatMap(pt => List(pt.x, pt.y, pt.z)).toArray, 0)
      // draw patch
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

  def rotate(degrees: Double, axis: Char): Unit = {
    axis match {
      case 'x' | 'X' => angleX += degrees
      case 'y' | 'Y' => angleY += degrees
      case 'z' | 'Z' => angleZ += degrees
    }
  }

  def scale(factor: Double): Unit = {
    scale += factor
  }

  def moveEye(direction: Char): Unit = {
    direction match {
      case 'l' => transX -= .05
      case 'r' => transX += .05
      case 'd' => transY -= .05
      case 'u' => transY += .05
      case 'f' => transZ += .05
      case 'b' => transZ -= .05
    }
  }

  def changeResolution(delta: Int) = {
    if (resolution > 1 && delta < 0) {
      resolution += delta
    } else if (resolution < 100 && delta > 0) {
      resolution += delta
    }
  }
}
