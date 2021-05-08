package com.intellij.plugin.gamification.listeners

import com.intellij.util.messages.Topic

interface GameEventListener {
    companion object {
        val TOPIC = Topic(GameEventListener::class.java)
    }

    /**
     * Called when a level has changed.
     */
    fun levelChanged(event: GameEvent) {}

    /**
     * Called when a progress has changed.
     */
    fun progressChanged(event: GameEvent) {}
}

data class GameEvent(val level: Int, val points: Int)
