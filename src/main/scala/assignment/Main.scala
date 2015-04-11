package assignment

import java.awt.{Color, BorderLayout, Dimension}
import javax.media.opengl.{GLCapabilities, GLProfile}
import javax.swing.{JFrame, JPanel}

object Main extends App {
  init()

  private def init(): Unit = {
    val frame = new JFrame()
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    val glp      = GLProfile.getDefault
    val caps     = new GLCapabilities(glp)

    val canvas = new MyCanvas(caps)
    val panel = new JPanel(new BorderLayout())
    panel.add(canvas, BorderLayout.CENTER)

    frame.add(panel)
    frame.setPreferredSize(new Dimension(1280, 768))
    frame.pack()
    frame.setVisible(true)
  }
}
