package com.github.turovkv.jetbrainsidefeaturesgamification.services

import com.github.turovkv.jetbrainsidefeaturesgamification.RewardStatistics
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

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
