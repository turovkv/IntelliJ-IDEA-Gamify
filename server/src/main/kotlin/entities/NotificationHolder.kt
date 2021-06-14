package com.intellij.gamify.server.entities

import com.intellij.gamify.server.entities.shared.Notification
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
data class NotificationHolder(
    val notification: Notification,
    val author: String,
    @Contextual val serverTime: Timestamp = Timestamp(System.currentTimeMillis()),
)
