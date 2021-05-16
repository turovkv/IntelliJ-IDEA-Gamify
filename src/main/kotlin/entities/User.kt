package com.intellij.gamify.server.entities

import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArraySet

data class User(
    val id: Int,
    var userInfo: UserInfo,

    var lastWatched: Timestamp = Timestamp(System.currentTimeMillis()),
    val notifications: Deque<NotificationWithTime> = ConcurrentLinkedDeque(),
    val subscribing: MutableSet<Int> = CopyOnWriteArraySet(),
)

data class UserInfo(
    val name: String,
    val displayName: String,
    val level: Int,
)
