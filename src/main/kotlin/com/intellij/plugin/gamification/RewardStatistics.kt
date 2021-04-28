package com.intellij.plugin.gamification

import com.intellij.internal.statistic.eventLog.LogEvent

class RewardStatistics {
    var level: Int = 0
    var pointsInLevel: Int = 400
    var explorationPoints: Int = 0
    var countExplorationUsages = HashMap<String, Int>()

    fun addEvent(logEvent: LogEvent) {
        val name = logEvent.event.data["id"].toString()
        countExplorationUsages[name] = countExplorationUsages.getOrDefault(name, 0) + 1
        explorationPoints += when (countExplorationUsages[name]) {
            1 -> 100
            2 -> 60
            3 -> 30
            4 -> 10
            else -> 0
        }
        level = explorationPoints / pointsInLevel
        println("level = " + level + " progress = " + getProgress() + " points = " + explorationPoints)
    }

    fun getProgress() = (100 * (explorationPoints % pointsInLevel)) / pointsInLevel

    fun clear() {
        level = 0
        explorationPoints = 0
        countExplorationUsages = HashMap()
    }
}
