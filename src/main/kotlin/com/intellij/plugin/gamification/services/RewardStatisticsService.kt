package com.intellij.plugin.gamification.services

import com.intellij.featureStatistics.ProductivityFeaturesRegistry
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.plugin.gamification.GameEvent
import com.intellij.plugin.gamification.PluginState
import com.intellij.plugin.gamification.RewardLogItem
import com.intellij.plugin.gamification.config.Logic
import com.intellij.plugin.gamification.listeners.GameEventListener

@State(
    name = "RewardStats",
    storages = [Storage("RewardStats.xml")]
)
class RewardStatisticsService : PersistentStateComponent<PluginState> {
    companion object {
        fun getInstance() = service<RewardStatisticsService>()
    }

    private var state = PluginState()

    private fun getPublisher() =
        ApplicationManager.getApplication().messageBus.syncPublisher(GameEventListener.TOPIC)

    fun addEvent(logEvent: LogEvent) {
        val name = logEvent.event.data["id"].toString()
        val oldCount = state.countFeatureUsages.getOrDefault(name, 0)
        val oldPoints = state.pointsPerFeature.getOrDefault(name, 0)
        val oldAllPoints = state.allPoints
        val newPoints = getPointsForEvent(name)

        state.allPoints += newPoints
        if (oldAllPoints != state.allPoints) {
            getPublisher().progressChanged(GameEvent(state.level, state.allPoints))
        }

        state.level = state.allPoints / state.pointsInLevel
        if (oldAllPoints / state.pointsInLevel != state.level) {
            getPublisher().levelChanged(GameEvent(state.level, state.allPoints))
        }

        state.countFeatureUsages[name] = oldCount + 1
        state.pointsPerFeature[name] = oldPoints + newPoints
    }

    private fun getPointsForEvent(name: String): Int {
        val count = state.countFeatureUsages.getOrDefault(name, 0)
        return if (count < Logic.NewPoints.arr.size) {
            Logic.NewPoints.arr[count]
        } else {
            0
        }
    }

    fun getProgress() = (Logic.maxProcess * (state.allPoints % state.pointsInLevel)) / state.pointsInLevel

    fun getRewardLog(): List<RewardLogItem> {
        return state.pointsPerFeature.map {
            val dname = getDisplayName(it.key)
            if (dname != null) {
                RewardLogItem(dname, it.value)
            } else {
                null
            }
        }.filterNotNull()
    }

    private fun getDisplayName(name: String) =
        ProductivityFeaturesRegistry.getInstance()?.getFeatureDescriptor(name)?.displayName


    fun clear() {
        state = PluginState()
    }

    override fun getState() = state

    override fun loadState(state: PluginState) {
        this.state = state
    }
}
