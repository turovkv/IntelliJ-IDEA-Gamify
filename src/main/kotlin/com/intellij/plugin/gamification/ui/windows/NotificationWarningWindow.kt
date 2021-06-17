package com.intellij.plugin.gamification.ui.windows

import javax.swing.JOptionPane
import javax.swing.JFrame

class NotificationWarningWindow {
    var frame = JFrame("WarningDialog")

    fun showWarning() {
        JOptionPane.showMessageDialog(
            frame,
            "You will not receive notifications. Please sign in to your account.",
            "Notifications Warning",
            JOptionPane.WARNING_MESSAGE
        )
    }
}
