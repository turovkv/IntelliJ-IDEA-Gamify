package com.intellij.gamify.server.entities.shared

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val text: String,
)
