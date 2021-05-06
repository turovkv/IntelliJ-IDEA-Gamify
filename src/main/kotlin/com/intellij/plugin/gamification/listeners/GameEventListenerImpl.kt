package com.intellij.plugin.gamification.listeners

import com.intellij.plugin.gamification.GameEvent

class GameEventListenerImpl : GameEventListener {
    override fun levelChanged(event: GameEvent) {
        println("Level Increased -> $event")
    }

    override fun progressChanged(event: GameEvent) {
        println("Progress Increased -> $event")
    }
}
