package com.intellij.plugin.gamification

import com.intellij.featureStatistics.ProductivityFeaturesRegistry
import com.intellij.internal.statistic.eventLog.LogEvent

class RewardStatistics {
    var points: Int = 0
    var level: Int = 0

    var countFeatureUsages: MutableMap<String, Int> = HashMap()
    var pointsPerFeature: MutableMap<String, Int> = HashMap()

    val pointsInLevel: Int = 400

    fun addEvent(logEvent: LogEvent) {
        val name = logEvent.event.data["id"].toString()
        val oldCount = countFeatureUsages.getOrDefault(name, 0)
        val oldPoints = pointsPerFeature.getOrDefault(name, 0)
        val newPoints = getPointsForEvent(name)

        points += newPoints
        level = points / pointsInLevel

        countFeatureUsages[name] = oldCount + 1
        pointsPerFeature[name] = oldPoints + newPoints
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

    fun getProgress() = (100 * (points % pointsInLevel)) / pointsInLevel

    fun clear() {
        level = 0
        points = 0
        countFeatureUsages = HashMap()
        pointsPerFeature = HashMap()
    }
}
