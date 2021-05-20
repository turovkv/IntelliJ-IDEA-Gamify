package com.intellij.plugin.gamification.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.*
import com.intellij.plugin.gamification.actions.GameStatisticsDialog
import com.intellij.ui.ClickListener
import com.intellij.ui.content.ContentFactory
import java.awt.event.MouseEvent

class GameProgressWidgetFactory : StatusBarWidgetFactory {
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

                var toolWindow = toolWindowManager.getToolWindow("My Progress")
                if (toolWindow == null) {
                    toolWindow = toolWindowManager.registerToolWindow("My Progress", true, ToolWindowAnchor.BOTTOM)

                    val contentManager = toolWindow.contentManager
                    val contentFactory = ContentFactory.SERVICE.getInstance()
                    contentManager.addContent(
                        contentFactory.createContent(
                            GameStatisticsDialog(project).contentPanel,
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
