package com.github.turovkv.jetbrainsidefeaturesgamification.listeners

import com.github.turovkv.jetbrainsidefeaturesgamification.services.SavedStatistics
import com.intellij.ide.AppLifecycleListener
import com.intellij.internal.statistic.eventLog.EventLogNotificationService
import com.intellij.internal.statistic.eventLog.LogEvent

internal class FeatureUsageListener : AppLifecycleListener {
    override fun appStarted() {
        EventLogNotificationService.subscribe(::subscriber , "FUS")
    }

    private fun subscriber(logEvent: LogEvent) {
        if (logEvent.group.id == "productivity" && logEvent.event.id == "feature.used") {
            SavedStatistics.get().addEvent(logEvent)
        }
    }
}
