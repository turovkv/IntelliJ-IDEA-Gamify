package com.intellij.plugin.gamification.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.EventQueue

class FeatureGamificationButton : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        ShowGameStatisticsDialog(getEventProject(event)).show()
    }
}
