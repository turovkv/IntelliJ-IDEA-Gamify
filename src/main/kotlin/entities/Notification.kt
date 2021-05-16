package com.intellij.gamify.server.entities

import java.sql.Timestamp

data class NotificationWithTime(
    val notification: Notification,
    val serverTime: Timestamp = Timestamp(System.currentTimeMillis()),
)

data class Notification(
    val text: String,
)
