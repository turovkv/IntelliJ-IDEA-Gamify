package com.intellij.gamify.server.entities

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val from: String,
    val to: String,
)
