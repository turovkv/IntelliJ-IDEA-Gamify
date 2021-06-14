package com.intellij.gamify.server.entities

import com.intellij.gamify.server.entities.shared.User
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
class UserHolder(
    val user: User,
    var passwordHash: ByteArray,
    @Contextual var lastWatched: Timestamp = Timestamp(System.currentTimeMillis())
)
