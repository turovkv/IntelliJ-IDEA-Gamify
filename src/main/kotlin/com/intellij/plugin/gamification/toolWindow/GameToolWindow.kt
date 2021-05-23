package com.intellij.plugin.gamification.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane

class GameToolWindow : ToolWindowFactory {
    private val tabPane = JBTabbedPane()
    private val panel1ID = "My Statistics"
    private val panel3ID = "Subscribe"
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val parent = toolWindow.component
        val subscribe = SubscribePanel()

        tabPane.add(panel1ID, MainFrame(project).contentPane)
        tabPane.add(panel3ID, subscribe.subPanel)

        parent.add(tabPane)
    }
}
