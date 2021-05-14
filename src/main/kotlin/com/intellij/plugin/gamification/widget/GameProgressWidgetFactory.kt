package com.intellij.plugin.gamification.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.plugin.gamification.actions.GameStatisticsDialog
import com.intellij.ui.ClickListener
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
                GameStatisticsDialog(project).show()
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
