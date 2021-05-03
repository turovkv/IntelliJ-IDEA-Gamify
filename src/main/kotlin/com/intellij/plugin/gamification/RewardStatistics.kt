package com.intellij.plugin.gamification

import com.intellij.featureStatistics.ProductivityFeaturesRegistry
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.plugin.gamification.listeners.GameEventListener

class RewardStatistics {
    val rewardStatisticsPublisher =
        ApplicationManager.getApplication().messageBus.syncPublisher(GameEventListener.TOPIC)

    var allPoints: Int = 0
    var level: Int = 0

    var countFeatureUsages: MutableMap<String, Int> = HashMap()
    var pointsPerFeature: MutableMap<String, Int> = HashMap()

    val pointsInLevel: Int = 400

    fun addEvent(logEvent: LogEvent) {
        val name = logEvent.event.data["id"].toString()
        val oldCount = countFeatureUsages.getOrDefault(name, 0)
        val oldPoints = pointsPerFeature.getOrDefault(name, 0)
        val oldAllPoints = allPoints
        val newPoints = getPointsForEvent(name)

        allPoints += newPoints
        if (oldAllPoints != allPoints) {
            rewardStatisticsPublisher.progressChanged()
        }

        level = allPoints / pointsInLevel
        if (oldAllPoints / pointsInLevel != level) {
            rewardStatisticsPublisher.levelChanged()
        }

        countFeatureUsages[name] = oldCount + 1
        pointsPerFeature[name] = oldPoints + newPoints
    }

    private fun getPointsForEvent(name: String): Int {
        return when (countFeatureUsages[name]) {
            0 -> 100
            1 -> 60
            2 -> 30
            3 -> 10
            else -> 0
        }
    }

    fun getProgress() = (100 * (allPoints % pointsInLevel)) / pointsInLevel

    fun getRewardLog(): List<RewardLogItem> {
        return pointsPerFeature.map {
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
        level = 0
        allPoints = 0
        countFeatureUsages = HashMap()
        pointsPerFeature = HashMap()
    }
}

data class RewardLogItem(var featureName: String, var points: Int)
