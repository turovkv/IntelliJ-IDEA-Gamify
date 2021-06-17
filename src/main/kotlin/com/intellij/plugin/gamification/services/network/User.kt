package com.intellij.plugin.gamification.services.network

data class User(
    var name: String = "No name",
    var userInfo: UserInfo = UserInfo()
)

data class UserInfo(
    var displayName: String = "No display name",
    var level: Int = 1,
    var progress: Int = 0,
)
