package com.intellij.plugin.gamification.config

object UI {
    object Dialog {
        const val with = 400
        const val height = 300
    }
}

object Logic {
    const val pointsInLevel = 400
    const val maxProgress = 100

    object NewPoints {
        const val v0 = 100
        const val v1 = 60
        const val v2 = 30
        const val v3 = 10
        val arr = arrayOf(v0, v1, v2, v3)
    }
}
