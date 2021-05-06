package com.intellij.plugin.gamification

import com.intellij.plugin.gamification.config.Logic

class GameMechanicsImpl : GameMechanics {
    override fun getPointsForEvent(eventName: String, state: PluginState): Int {
        val count = state.countFeatureUsages.getOrDefault(eventName, 0)
        return if (count < Logic.NewPoints.arr.size) {
            Logic.NewPoints.arr[count]
        } else {
            0
        }
    }

    override fun maxPointsOnLevel(curLevel: Int): Int {
        return Logic.pointsInLevel
    }
}
