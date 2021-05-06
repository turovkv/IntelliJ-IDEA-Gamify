package com.intellij.plugin.gamification.listeners

import com.intellij.ide.AppLifecycleListener
import com.intellij.internal.statistic.eventLog.EventLogNotificationService
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.plugin.gamification.services.RewardStatisticsService

internal class FeatureUsageListener : AppLifecycleListener {
    override fun appStarted() {
        EventLogNotificationService.subscribe(::subscriber, "FUS")
    }

    private fun subscriber(logEvent: LogEvent) {
        if (logEvent.group.id == "productivity" && logEvent.event.id == "feature.used") {
            RewardStatisticsService.getInstance().addEvent(logEvent)
        }
    }
}
