package com.intellij.plugin.gamification

import com.intellij.plugin.gamification.config.Logic

class GameMechanicsImpl : GameMechanics {
    override fun getPointsForEvent(countUsages: Int): Int {
        return if (countUsages < Logic.NewPoints.arr.size) {
            Logic.NewPoints.arr[countUsages]
        } else {
            0
        }
    }

    override fun maxPointsOnLevel(curLevel: Int): Int {
        return Logic.pointsInLevel
    }
}
