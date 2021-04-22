package com.github.turovkv.jetbrainsidefeaturesgamification.listeners

import com.github.turovkv.jetbrainsidefeaturesgamification.RewardStatistics
import com.github.turovkv.jetbrainsidefeaturesgamification.services.SavedStatistics
import com.intellij.ide.AppLifecycleListener
import com.intellij.internal.statistic.eventLog.EventLogNotificationService
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

internal class FeatureUsageListener : AppLifecycleListener {
    override fun appStarting(projectFromCommandLine: Project?) {
        println("Lets Go!")
        EventLogNotificationService.subscribe({ println(it) }, "GameSys") //Not working :(
        //EventLogNotificationService.subscribe(::subscriber , "GameSys")
    }

    fun subscriber(logEvent: LogEvent) {
        if (logEvent.group.id == "productivity" && logEvent.event.id == "feature.used") {
            service<SavedStatistics>().state.addReward(logEvent.toString(), RewardStatistics.DEFAULT_REWARD)
        }
    }
}
