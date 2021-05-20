package com.intellij.plugin.gamification.widget

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.plugin.gamification.actions.GameStatisticsDialog
import com.intellij.ui.ClickListener
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory

import java.awt.event.MouseEvent

class GameProgressWidgetFactory : StatusBarWidgetFactory, DumbAware {
    override fun getId(): String {
        return GameProgressPanel.WIDGET_ID
    }

    override fun getDisplayName(): String {
        return "Game progress"
    }

    override fun isAvailable(project: Project): Boolean {
        return true
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun createWidget(project: Project): StatusBarWidget {
        val panel = GameProgressPanel()
        object : ClickListener() {
            override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
//                GameStatisticsDialog(project).show()
                val toolWindowManager = ToolWindowManager.getInstance(project)
                val toolWindowID = "Feature Registry"
                val panel1ID = "Sign In"
                val panel2ID = "My Statistics"
                val panel3ID = "Subscribe"

                var toolWindow = toolWindowManager.getToolWindow(toolWindowID)
                if (toolWindow == null) {
                    toolWindow = toolWindowManager.registerToolWindow(toolWindowID, true, ToolWindowAnchor.BOTTOM)

                    val contentManager = toolWindow.contentManager
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
                toolWindow.activate(null)
                return true
            }
        }.installOn(panel, true)
        return panel
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        return true
    }
}
