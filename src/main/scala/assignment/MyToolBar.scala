package assignment

import javax.swing._

import utils._

class MyToolBar extends JToolBar() {
  private var loadBtnClickListener : Option[String => Unit] = None

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  add(toolbar1())

  private def toolbar1() = {
    val content = new JPanel()
    val filePathField = new JTextField("pyramid_05.txt", 10)
    val fc = new JFileChooser("./")
    fc.onAction(filePathField.setText(fc.getSelectedFile.getAbsolutePath))

    content.add(new JLabel("Filename:"))
    content.add(filePathField)
    content.add(new JButton("Browse").onAction(fc.showOpenDialog(this)))
    content.add(new JButton("Load").onAction(loadBtnClickListener.notify(filePathField.getText)))
    content
  }

  def onLoadButtonClick(listener: String => Unit) = loadBtnClickListener = Some(listener)

  implicit class Option2Notify[T](option: Option[T => Unit]) {
    def notify(t: T) = option.map(_(t))
  }
}

