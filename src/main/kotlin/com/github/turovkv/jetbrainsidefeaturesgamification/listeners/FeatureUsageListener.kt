package com.github.turovkv.jetbrainsidefeaturesgamification.listeners

import com.github.turovkv.jetbrainsidefeaturesgamification.services.SavedStatistics
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages

internal class FeatureUsageListener : AnActionListener {
    override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        val stats = service<SavedStatistics>().state
        stats.addReward(action.toString(), 1)
        if (stats.accumulatedPoints > 10) {
            Messages.showMessageDialog(stats.rewards.toString(), "Just Saying", Messages.getInformationIcon())
            stats.clear()
        }
    }
}
