package com.intellij.plugin.gamification.listeners

import java.util.EventListener

interface GameEventListener : EventListener {

    /**
     * Called when a level has changed.
     */
    fun levelChanged(event: GameEvent) {}

    /**
     * Called when a progress has changed.
     */
    fun progressChanged(event: GameEvent) {}
}

data class GameEvent(val level: Int, val progress: Int)
