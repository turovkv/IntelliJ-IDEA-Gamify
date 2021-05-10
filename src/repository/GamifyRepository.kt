package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo

interface GamifyRepository {
    fun getAllUsers(): List<User>

    fun getUser(id: Int): User?

    fun addUser(userInfo: UserInfo): User

    fun removeUser(id: Int): Boolean

    fun updateUser(id: Int, userInfo: UserInfo): Boolean
}
