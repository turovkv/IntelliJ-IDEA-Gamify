package com.intellij.plugin.gamification.mechanics

interface GameMechanics {
    fun getPointsForEvent(countUsages: Int): Int
    fun maxPointsOnLevel(level: Int): Int
    fun getProgress(pointsOnLevel: Int, level: Int): Int
}
