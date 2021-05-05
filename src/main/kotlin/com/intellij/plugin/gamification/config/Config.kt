package com.intellij.plugin.gamification

object UI {
    object Bounds {
        const val x = 0
        const val y = 0
        const val width = 300
        const val height = 125
    }

    object Border {
        const val top = 5
        const val left = 5
        const val bottom = 5
        const val right = 5
    }

    object Layout {
        const val hgap = 0
        const val vgap = 0
    }
}

object Logic {
    const val pointsInLevel = 400
    const val maxProcess = 100

    object NewPoints {
        const val v0 = 100
        const val v1 = 60
        const val v2 = 30
        const val v3 = 10
        val arr = arrayOf(v0, v1, v2, v3)
    }
}
