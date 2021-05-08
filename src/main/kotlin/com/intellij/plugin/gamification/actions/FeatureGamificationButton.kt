package com.intellij.plugin.gamification.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class FeatureGamificationButton : AnAction(), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        ShowGameStatisticsDialog(null).show()
    }
}
