package com.intellij.plugin.gamification

import com.intellij.openapi.application.ApplicationManager
import com.intellij.plugin.gamification.config.Logic
import com.intellij.plugin.gamification.listeners.GameEventListener

class PluginState {

    var allPoints: Int = 0
    var level: Int = 0

    var countFeatureUsages: MutableMap<String, Int> = HashMap()
    var pointsPerFeature: MutableMap<String, Int> = HashMap()

    val pointsInLevel: Int = Logic.pointsInLevel

}

data class RewardLogItem(var featureName: String, var points: Int)

data class GameEvent(val level: Int, val points: Int)