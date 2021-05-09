package com.intellij.plugin.gamification.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

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
        return GameProgressPanel()
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        return true
    }
}
