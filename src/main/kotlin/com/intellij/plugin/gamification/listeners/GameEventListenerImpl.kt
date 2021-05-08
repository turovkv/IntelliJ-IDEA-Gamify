package com.intellij.plugin.gamification.listeners

class GameEventListenerImpl : GameEventListener {
    override fun levelChanged(event: GameEvent) {
        println("Level Increased -> $event")
    }

    override fun progressChanged(event: GameEvent) {
        println("Progress Increased -> $event")
    }
}
