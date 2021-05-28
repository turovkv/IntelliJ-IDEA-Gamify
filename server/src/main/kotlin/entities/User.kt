package com.intellij.gamify.server.entities

data class User(
    val name: String,
    var userInfo: UserInfo = UserInfo()
)

data class UserInfo(
    val displayName: String = "No display name",
    val level: Int = 1,
    val progress: Int = 0,
)
