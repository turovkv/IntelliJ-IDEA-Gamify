package com.intellij.gamify.server.entities

import com.intellij.gamify.server.entities.shared.User
import kotlinx.serialization.Serializable

@Serializable
class UserHolder(
    val user: User,
    var passwordHash: ByteArray,
    var lastWatched: Long = System.currentTimeMillis()
)
