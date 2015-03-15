package assignment

import javax.media.opengl.{GL, GLAutoDrawable, GLEventListener}

/**
 * Created by thangiee on 3/14/15.
 */
class SimpleScence extends GLEventListener {
  override def init(glAutoDrawable: GLAutoDrawable): Unit = {

  }

  override def display(glAutoDrawable: GLAutoDrawable): Unit = {
    update()
    render(glAutoDrawable)
  }

  override def reshape(glAutoDrawable: GLAutoDrawable, i: Int, i1: Int, i2: Int, i3: Int): Unit = {

  }

  override def dispose(glAutoDrawable: GLAutoDrawable): Unit = {

  }

  def update(): Unit = {}

  def render(drawable: GLAutoDrawable): Unit = {
    val gl = drawable.getGL.getGL2

    // draw a triangle filling the window
    gl.glBegin(GL.GL_TRIANGLES)
    gl.glColor3f(1, 0, 0)
    gl.glVertex2f(-1, -1)
    gl.glColor3f(0, 1, 0)
    gl.glVertex2f(0, 1)
    gl.glColor3f(0, 0, 1)
    gl.glVertex2f(1, -1)
    gl.glEnd()
  }
}
