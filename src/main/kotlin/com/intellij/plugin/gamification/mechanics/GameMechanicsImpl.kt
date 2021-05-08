package com.intellij.plugin.gamification.mechanics

class GameMechanicsImpl : GameMechanics {
    override fun getPointsForEvent(countUsages: Int): Int {
        return if (countUsages < NewPoints.arr.size) {
            NewPoints.arr[countUsages]
        } else {
            0
        }
    }

    override fun maxPointsOnLevel(level: Int): Int {
        return pointsInLevel
    }

    override fun getProgress(pointsOnLevel: Int, level: Int): Int {
        return (maxProgress * pointsOnLevel) / maxPointsOnLevel(level)
    }

    private companion object {
        const val pointsInLevel = 400
        const val maxProgress = 100

        object NewPoints {
            private const val v0 = 100
            private const val v1 = 60
            private const val v2 = 30
            private const val v3 = 10
            val arr = arrayOf(v0, v1, v2, v3)
        }
    }
}
