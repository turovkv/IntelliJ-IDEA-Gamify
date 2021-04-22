package com.github.turovkv.jetbrainsidefeaturesgamification

class RewardStatistics {
    data class Reward(var name: String = "", var points: Int = 0)

    var accumulatedPoints: Int = 0
    var rewards = HashSet<Reward>()

    companion object {
        const val DEFAULT_REWARD: Int = 1
    }

    fun addReward(name: String, points: Int) {
        accumulatedPoints += points
        rewards.add(Reward(name, points))
    }

    fun clear() {
        accumulatedPoints = 0
        rewards = HashSet()
    }
}
