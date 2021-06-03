package com.intellij.plugin.gamification.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.plugin.gamification.actions.GameStatisticsDialog
import java.awt.CardLayout
import java.awt.event.ActionListener
import javax.swing.JPanel

class MainFrame(project: Project) {
    private var cardLayout: CardLayout? = null
    val panel = JPanel()

    init {
        cardLayout = CardLayout()
        panel.layout = cardLayout

        val firstPage: JPanel = SignInPanel {
            cardLayout!!.show(
                panel,
                "SECOND"
            )
        }.signPanel

        panel.add(firstPage, "FIRST")

        val sndListener = ActionListener {
            cardLayout!!.show(
                panel,
                "FIRST"
            )
        }
        val secondPage: JPanel = GameStatisticsDialog(project, sndListener).splitter

        panel.add(secondPage, "SECOND")

        cardLayout!!.first(panel)
    }
}
