package com.github.turovkv.jetbrainsidefeaturesgamification.services

import com.github.turovkv.jetbrainsidefeaturesgamification.RewardStatistics
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "RewardStats",
    storages = [Storage("RewardStats.xml")]
)
class SavedStatistics : PersistentStateComponent<RewardStatistics> {
    var saved = RewardStatistics()

    override fun getState(): RewardStatistics {
        return saved
    }

    override fun loadState(state: RewardStatistics) {
        this.saved = state
    }
}
