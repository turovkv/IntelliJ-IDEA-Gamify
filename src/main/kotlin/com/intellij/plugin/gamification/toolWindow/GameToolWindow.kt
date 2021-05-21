package com.intellij.plugin.gamification.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.plugin.gamification.actions.GameStatisticsDialog
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory

class GameToolWindow(project: Project) {

    private val toolWindowManager = ToolWindowManager.getInstance(project)
    private val toolWindowID = "Feature Registry"
    private val panel1ID = "Sign In"
    private val panel2ID = "My Statistics"
    private val panel3ID = "Subscribe"

    private var toolWindow = toolWindowManager.getToolWindow(toolWindowID)

    init {
        if (toolWindow == null) {
            toolWindow = toolWindowManager.registerToolWindow(toolWindowID, true, ToolWindowAnchor.BOTTOM)

            val contentManager = toolWindow!!.contentManager
            val contentFactory = ContentFactory.SERVICE.getInstance()

            val tabPane = JBTabbedPane()
            val signIn = SignInPanel()
            val subscribe = SubscribePanel()

            tabPane.add(panel1ID, signIn.signPanel)
            tabPane.add(panel2ID, GameStatisticsDialog(project).contentPanel)
            tabPane.add(panel3ID, subscribe.subPanel)

            contentManager.addContent(
                contentFactory.createContent(
                    tabPane,
                    "",
                    false
                )
            )
        }
    }

    fun addWindow() {
        toolWindow?.activate(null)
    }
}
