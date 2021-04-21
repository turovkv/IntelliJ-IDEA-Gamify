package com.github.turovkv.jetbrainsidefeaturesgamification

class RewardStatistics {
    data class Reward(val name: String, val points: Int)

    var accumulatedPoints: Int = 0
    var rewards = HashSet<Reward>()

    fun addReward(name: String, points: Int) {
        accumulatedPoints += points
        rewards.add(Reward(name, points))
    }

    fun clear() {
        accumulatedPoints = 0
        rewards = HashSet()
    }
}