package com.intellij.plugin.gamification.ui.windows

import javax.swing.JFrame
import javax.swing.JOptionPane

class NotificationWarningWindow(warnName: String, warnText: String) {
    var frame = JFrame("WarningDialog")
    private val text = warnText
    private val name = warnName

    fun showWarning() {
        JOptionPane.showMessageDialog(
            frame,
            text,
            name,
            JOptionPane.WARNING_MESSAGE
        )
    }
}
