package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserDraft

class InMemoryGamifyRepository : GamifyRepository {

    private val users = mutableListOf<User>()

    override fun getAllUsers(): List<User> {
        return users
    }

    override fun getUser(id: Int): User? {
        return users.firstOrNull { it.id == id }
    }

    override fun addUser(draft: UserDraft): User {
        val user = User(
            id = users.size + 1,
            name = draft.name,
            points = draft.points
        )
        users.add(user)
        return user
    }

    override fun removeUser(id: Int): Boolean {
        return users.removeIf { it.id == id }
    }

    override fun updateUser(id: Int, draft: UserDraft): Boolean {
        val user = users.firstOrNull { it.id == id }
            ?: return false

        user.name = draft.name
        user.points = draft.points
        return true
    }
}
