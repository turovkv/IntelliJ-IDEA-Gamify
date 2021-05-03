package com.intellij.plugin.gamification

import com.intellij.featureStatistics.ProductivityFeaturesRegistry
import com.intellij.internal.statistic.eventLog.LogEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.plugin.gamification.listeners.GameEventListener

class RewardStatistics {
    private val rewardStatisticsPublisher =
        ApplicationManager.getApplication().messageBus.syncPublisher(GameEventListener.TOPIC)

    var allPoints: Int = 0
    var level: Int = 0

    var countFeatureUsages: MutableMap<String, Int> = HashMap()
    var pointsPerFeature: MutableMap<String, Int> = HashMap()

    val pointsInLevel: Int = 400

    fun addEvent(logEvent: LogEvent) {
        println(rewardStatisticsPublisher)

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

        println("hello")
        clear()
    }

    private fun getPointsForEvent(name: String): Int {
        return when (countFeatureUsages[name]) {
            1 -> 100
            2 -> 60
            3 -> 30
            4 -> 10
            else -> 0
        }
    }

    fun getRewardList(): List<List<String>> {
        return pointsPerFeature.map {
            val dname = getDisplayName(it.key)
            if (dname != null) {
                listOf(dname, it.value.toString())
            } else {
                null
            }
        }.filterNotNull()
    }

    private fun getDisplayName(name: String) =
        ProductivityFeaturesRegistry.getInstance()?.getFeatureDescriptor(name)?.displayName

    fun getProgress() = (100 * (allPoints % pointsInLevel)) / pointsInLevel

    fun clear() {
        level = 0
        allPoints = 0
        countFeatureUsages = HashMap()
        pointsPerFeature = HashMap()
    }
}
