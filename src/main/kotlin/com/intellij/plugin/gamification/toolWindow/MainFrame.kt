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
                "FIRST"
            )
        }.signPanel

        val fstListener = ActionListener {
            cardLayout!!.show(
                panel,
                "SECOND"
            )
        }

        val secondPage: JPanel = GameStatisticsDialog(project, fstListener).splitter

        panel.add(secondPage, "FIRST")
        panel.add(firstPage, "SECOND")

        cardLayout!!.first(panel)
    }
}
