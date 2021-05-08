package com.intellij.plugin.gamification.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import java.awt.EventQueue

class FeatureGamificationButton : AnAction(), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        ShowGameStatisticsDialog(getEventProject(event)).show()
    }
}
