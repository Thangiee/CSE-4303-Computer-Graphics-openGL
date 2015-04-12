package assignment

import java.awt.event.{AWTEventListener, KeyEvent}
import java.awt.{AWTEvent, BorderLayout, Dimension}
import javax.swing.{JFileChooser, JFrame, JPanel}

import assignment.Projection.{Parallel, Perspective}
import utils._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object Main extends App with AWTEventListener {
  private val vertexes      = new ListBuffer[Vertex]()
  private val faces         = new ListBuffer[Face]()
  private val cameras       = new ListBuffer[Camera]()
  private var inputFile     = "patches_06.txt"
  private var resolution    = 4
  private val ctrlPoints = ArrayBuffer[Double]()

  private val frame = new JFrame()
  private var canvas = new MyCanvas(vertexes, faces, cameras, ctrlPoints, resolution)
  private val panel  = new JPanel(new BorderLayout())

  init()

  def init(): Unit = {
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

    panel.add(canvas, BorderLayout.CENTER)

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.getToolkit.addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK)
    frame.add(panel)
    frame.setPreferredSize(new Dimension(1280, 768))
    frame.pack()
    frame.setVisible(true)
  }

  override def eventDispatched(event: AWTEvent): Unit = {
    event match {
      case e: KeyEvent => e.getID match {
        case KeyEvent.KEY_TYPED => keyTyped(e)
        case KeyEvent.KEY_PRESSED => keyPressed(e)
        case _ => e.consume()
      }
    }
  }

  private def keyTyped(e: KeyEvent): Unit = {
    e.getKeyChar match {
      case 'n' => promptFilename()
      case 'd' => loadAndDisplay()
      case 'x' => canvas.rotate(5, 'x')
      case 'X' => canvas.rotate(-5, 'x')
      case 'y' => canvas.rotate(5, 'y')
      case 'Y' => canvas.rotate(-5, 'y')
      case 'z' => canvas.rotate(5, 'z')
      case 'Z' => canvas.rotate(-5, 'z')
      case 's' => canvas.scale(.05)
      case 'S' => canvas.scale(-.05)
      case 'f' => canvas.moveEye('f')
      case 'b' => canvas.moveEye('b')
      case 'r' => canvas.changeResolution(-1)
      case 'R' => canvas.changeResolution(1)
      case 'p' =>
      case _ =>
    }
  }

  private def keyPressed(e: KeyEvent): Unit = {
    e.getKeyCode match {
      case KeyEvent.VK_LEFT => canvas.moveEye('l')
      case KeyEvent.VK_RIGHT => canvas.moveEye('r')
      case KeyEvent.VK_UP => canvas.moveEye('u')
      case KeyEvent.VK_DOWN => canvas.moveEye('d')
      case _ =>
    }
  }

  private def promptFilename(): Unit = {
    val fc = new JFileChooser("./")
    if (fc.showOpenDialog(panel) == 0) inputFile = fc.getSelectedFile.getAbsolutePath
  }

  private def loadAndDisplay(): Unit = {
    println(s"load $inputFile")

    Seq(vertexes, faces, ctrlPoints).foreach(_.clear())
    io.Source.fromFile(inputFile).getLines().mkString("\n").split("\n").foreach { line =>
      line.head match {
        case 'n' => resolution = parseInt(line)
        case 'b' => ctrlPoints ++= parseControlPoint(line)
        case 'v' => vertexes += parseVertex(line)
        case 'f' => faces += parseFace(line)
        case _   =>
      }
    }

    panel.remove(canvas)
    canvas = new MyCanvas(vertexes, faces, cameras, ctrlPoints, resolution)
    panel.add(canvas)
    frame.pack()
    panel.requestFocus()
  }
}
