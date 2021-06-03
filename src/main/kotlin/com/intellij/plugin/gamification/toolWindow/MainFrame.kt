package com.intellij.plugin.gamification.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.plugin.gamification.actions.GameStatisticsDialog
import java.awt.CardLayout
import java.awt.event.ActionListener
import javax.swing.JFrame
import javax.swing.JPanel

class MainFrame(project: Project) : JFrame() {

    init {
        title = "Swing Application"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        isLocationByPlatform = true

        val cLayout = CardLayout()
        layout = cLayout

        val sndListener = ActionListener {
            cLayout.show(
                contentPane,
                "FIRST"
            )
        }

        val secondPage: JPanel = GameStatisticsDialog(project, sndListener).splitter
        add("SECOND", secondPage)

        val firstPage: JPanel = SignInPanel {
            cLayout.show(
                contentPane,
                "SECOND"
            )
        }.signPanel
        add("FIRST", firstPage)
        cLayout.show(contentPane, "FIRST")
        isVisible = true
    }
}
