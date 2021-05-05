package com.intellij.plugin.gamification.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.plugin.gamification.RewardStatistics

@State(
    name = "RewardStats",
    storages = [Storage("RewardStats.xml")]
)
class SavedStatistics : PersistentStateComponent<RewardStatistics> {
    var saved = RewardStatistics()

    companion object {
        fun get() = service<SavedStatistics>().state
    }

    override fun getState() = saved

    override fun loadState(state: RewardStatistics) {
        this.saved = state
    }
}
