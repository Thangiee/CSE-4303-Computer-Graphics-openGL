package assignment

import java.awt.Frame
import javax.media.opengl.awt.GLCanvas
import javax.media.opengl.{GLProfile, GLCapabilities}

object Main extends App {
  val glp = GLProfile.getDefault
  val caps = new GLCapabilities(glp)
  val canvas = new GLCanvas(caps)
  canvas.addGLEventListener(new SimpleScence)

  val frame = new Frame()
  frame.setSize(300, 300)
  frame.add(canvas)
  frame.setVisible(true)
}
