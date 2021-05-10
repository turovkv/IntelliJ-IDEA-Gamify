package com.intellij.gamify.server.entities

data class User(
    val id: Int,
    val userInfo: UserInfo
)

data class UserInfo(
    var name: String,
    var points: Int
)