package com.intellij.plugin.gamification.listeners

import com.intellij.ide.AppLifecycleListener
import com.intellij.internal.statistic.eventLog.EventLogNotificationService
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.plugin.gamification.services.RewardStatisticsService
import com.intellij.plugin.gamification.services.network.NetworkService

internal class FeatureUsageListener : AppLifecycleListener {
    override fun appStarted() {
        EventLogNotificationService.subscribe(::subscriber, "FUS")

        NetworkService
            .getInstance()
            .launchNotificationReceiver()
    }

    private fun subscriber(logEvent: LogEvent) {
        if (logEvent.group.id == "productivity" && logEvent.event.id == "feature.used") {
            println(logEvent.event.data["id"].toString())
            RewardStatisticsService.getInstance().addEvent(logEvent)
        }
    }
}
