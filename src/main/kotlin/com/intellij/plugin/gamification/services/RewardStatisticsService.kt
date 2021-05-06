package com.intellij.plugin.gamification.services

import com.intellij.featureStatistics.ProductivityFeaturesRegistry
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.plugin.gamification.GameEvent
import com.intellij.plugin.gamification.GameMechanics
import com.intellij.plugin.gamification.GameMechanicsImpl
import com.intellij.plugin.gamification.RewardInfoItem
import com.intellij.plugin.gamification.PluginState
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
    private val mechanics: GameMechanics = GameMechanicsImpl()

    private fun getPublisher() =
        ApplicationManager.getApplication().messageBus.syncPublisher(GameEventListener.TOPIC)

    fun addEvent(logEvent: LogEvent) {
        val name = logEvent.event.data["id"].toString()
        val oldCount = state.countFeatureUsages.getOrDefault(name, 0)
        val oldPoints = state.pointsPerFeature.getOrDefault(name, 0)
        val oldLevel = state.level
        val addPoints = mechanics.getPointsForEvent(name, state)

        state.allPoints += addPoints
        state.pointsOnLevel += addPoints

        while (state.pointsOnLevel >= mechanics.maxPointsOnLevel(state.level)) {
            state.pointsOnLevel -= mechanics.maxPointsOnLevel(state.level)
            state.level += 1
        }

        if (oldLevel != state.level) {
            getPublisher().levelChanged(GameEvent(state.level, state.allPoints))
        }

        if (addPoints != 0) {
            getPublisher().progressChanged(GameEvent(state.level, state.allPoints))
        }

        state.countFeatureUsages[name] = oldCount + 1
        state.pointsPerFeature[name] = oldPoints + addPoints
    }

    fun getProgress() =
        (Logic.maxProgress * state.pointsOnLevel) / mechanics.maxPointsOnLevel(state.level)

    fun getRewardInfo(): List<RewardInfoItem> {
        return state.pointsPerFeature.map {
            val dname = getDisplayName(it.key)
            if (dname != null) {
                RewardInfoItem(dname, it.value)
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
