package com.intellij.plugin.gamification.services

import com.intellij.featureStatistics.ProductivityFeaturesRegistry
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.plugin.gamification.listeners.GameEvent
import com.intellij.plugin.gamification.listeners.GameEventListener
import com.intellij.plugin.gamification.mechanics.GameMechanics
import com.intellij.plugin.gamification.mechanics.GameMechanicsImpl
import com.intellij.util.EventDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@State(
    name = "RewardStats",
    storages = [Storage("RewardStats.xml")]
)
@Suppress("TooManyFunctions")
class RewardStatisticsService : PersistentStateComponent<RewardStatisticsService.PluginState> {
    class PluginState {
        var allPoints: Int = 0
        var pointsOnLevel: Int = 0
        var level: Int = 0

        var countFeatureUsages: MutableMap<String, Int> = HashMap()
        var pointsPerFeature: MutableMap<String, Int> = HashMap()
    }

    companion object {
        fun getInstance() = service<RewardStatisticsService>()
    }

    private var state = PluginState()
    private val mechanics: GameMechanics = GameMechanicsImpl()

    private val gameEventDispatcher = EventDispatcher.create(
        GameEventListener::class.java
    )

    private fun getPublisher() = gameEventDispatcher.multicaster

    fun addListener(listener: GameEventListener, disposable: Disposable) {
        gameEventDispatcher.addListener(listener, disposable)
    }

    fun addEvent(logEvent: LogEvent) {
        val name = logEvent.event.data["id"].toString()
        val oldCount = state.countFeatureUsages.getOrDefault(name, 0)
        val oldPoints = state.pointsPerFeature.getOrDefault(name, 0)
        val oldLevel = state.level
        val countUsages = state.countFeatureUsages.getOrDefault(name, 0)
        val addPoints = mechanics.getPointsForEvent(countUsages)

        state.allPoints += addPoints
        state.pointsOnLevel += addPoints

        while (state.pointsOnLevel >= mechanics.maxPointsOnLevel(state.level)) {
            state.pointsOnLevel -= mechanics.maxPointsOnLevel(state.level)
            state.level += 1
        }

        state.countFeatureUsages[name] = oldCount + 1
        state.pointsPerFeature[name] = oldPoints + addPoints

        if (oldLevel != state.level) {
            getPublisher().levelChanged(getCurrentGameEvent())
        }

        if (addPoints != 0) {
            getPublisher().progressChanged(getCurrentGameEvent())
        }
        GlobalScope.launch {
            try {
                NetworkService.getInstance().addNotification(Notification(name))
            } catch (e: NetworkServiceException) {
                println("Error! -> " + e.localizedMessage)
            }
        }
    }

    fun getCurrentGameEvent() = GameEvent(getLevel(), getProgress())

    fun getLevel() = state.level

    fun getProgress() =
        (GameMechanicsImpl.maxProgress * state.pointsOnLevel) / mechanics.maxPointsOnLevel(state.level)

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
        getPublisher().progressChanged(getCurrentGameEvent())
        getPublisher().levelChanged(getCurrentGameEvent())
    }

    override fun getState() = state

    override fun loadState(state: PluginState) {
        this.state = state
    }
}

data class RewardInfoItem(var featureName: String, var points: Int)
