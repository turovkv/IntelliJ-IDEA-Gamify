package com.intellij.plugin.gamification.listeners

class GameEventListenerImpl : GameEventListener {
    override fun levelChanged() {
        println("Level Increased")
    }

    override fun progressChanged() {
        println("Progress Increased")
    }
}
