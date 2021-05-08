package com.intellij.plugin.gamification

interface GameMechanics {
    fun getPointsForEvent(countUsages: Int): Int
    fun maxPointsOnLevel(curLevel: Int): Int
}
