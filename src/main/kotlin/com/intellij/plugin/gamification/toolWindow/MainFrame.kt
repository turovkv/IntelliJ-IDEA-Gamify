package com.intellij.plugin.gamification.toolWindow

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.plugin.gamification.actions.GameStatisticsDialog
import com.intellij.plugin.gamification.services.network.ClientException
import com.intellij.plugin.gamification.services.network.NetworkService
import kotlinx.coroutines.runBlocking
import java.awt.CardLayout
import java.awt.event.ActionListener
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField

class MainFrame(project: Project) : JFrame() {

    init {
        title = "Swing Application"
//        setSize(1200, 800)
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

        val listener = ActionListener {
            val login = (contentPane.getComponent(1) as JTextField).text
            val password = (contentPane.getComponent(3) as JPasswordField).password.toString()

            try {
                runBlocking {
                    NetworkService.getInstance().signUp(login, password)
                }
                cLayout.show(
                    contentPane,
                    "SECOND"
                )
            } catch (e: ClientException) {
                Logger
                    .getFactory()
                    .getLoggerInstance("Gamify")
                    .error(e)
                println(e.localizedMessage)
            }
        }

        val firstPage: JPanel = SignInPanel(listener).signPanel
        add("FIRST", firstPage)
        cLayout.show(contentPane, "FIRST")
        isVisible = true
    }
}
