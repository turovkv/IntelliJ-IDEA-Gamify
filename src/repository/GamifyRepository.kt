package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserDraft

interface GamifyRepository {
    fun getAllUsers(): List<User>

    fun getUser(id: Int): User?

    fun addUser(draft: UserDraft): User

    fun removeUser(id: Int): Boolean

    fun updateUser(id: Int, draft: UserDraft): Boolean
}
