package com.intellij.gamify.server.entities

import com.intellij.gamify.server.entities.shared.Notification
import kotlinx.serialization.Serializable

@Serializable
data class NotificationHolder(
    val notification: Notification,
    val author: String,
    val serverTime: Long = System.currentTimeMillis(),
)
