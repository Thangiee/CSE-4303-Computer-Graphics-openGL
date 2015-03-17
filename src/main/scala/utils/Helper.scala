package utils

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JFileChooser, JButton}

trait Helper {

  implicit class JButtonHelper(btn: JButton) {
    def onAction(f: => Unit): JButton = {
      btn.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent): Unit = f
      })
      btn
    }
  }

  implicit class JFileChooserHelper(fc: JFileChooser) {
    def onAction(f: => Unit): JFileChooser = {
      fc.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent): Unit = f
      })
      fc
    }
  }
}

object Helper extends Helper
