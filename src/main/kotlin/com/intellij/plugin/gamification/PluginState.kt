package com.intellij.plugin.gamification

class PluginState {
    var allPoints: Int = 0
    var pointsOnLevel: Int = 0
    var level: Int = 0

    var countFeatureUsages: MutableMap<String, Int> = HashMap()
    var pointsPerFeature: MutableMap<String, Int> = HashMap()
}

data class RewardInfoItem(var featureName: String, var points: Int)

data class GameEvent(val level: Int, val points: Int)
