package com.intellij.gamify.server.entities

data class User(
    val id: Int,
    var name: String,
    var points: Int
)
