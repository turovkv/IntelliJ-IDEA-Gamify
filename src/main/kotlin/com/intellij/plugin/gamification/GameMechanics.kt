package com.intellij.plugin.gamification

interface GameMechanics {
    fun getPointsForEvent(eventName: String, state: PluginState): Int
    fun maxPointsOnLevel(curLevel: Int): Int
}
