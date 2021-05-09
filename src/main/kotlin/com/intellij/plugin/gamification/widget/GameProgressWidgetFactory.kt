package com.intellij.plugin.gamification.widget

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.plugin.gamification.listeners.GameEvent
import com.intellij.plugin.gamification.listeners.GameEventListener
import com.intellij.plugin.gamification.services.RewardStatisticsService

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

        panel.updateState(
            RewardStatisticsService.getInstance().getCurrentGameEvent()
        )

        RewardStatisticsService
            .getInstance()
            .addListener(
                object : GameEventListener {
                    override fun levelChanged(event: GameEvent) {
                        panel.updateState(event)
                    }

                    override fun progressChanged(event: GameEvent) {
                        panel.updateState(event)
                    }
                }
            )

        return panel
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        return true
    }
}
