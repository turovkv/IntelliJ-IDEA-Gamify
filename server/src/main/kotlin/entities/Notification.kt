package com.intellij.gamify.server.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
data class NotificationWithTime(
    val notification: Notification,
    @Contextual
    val serverTime: Timestamp = Timestamp(System.currentTimeMillis()),
)

@Serializable
data class Notification(
    val text: String,
)
