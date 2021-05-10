package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo

class InMemoryGamifyRepository : GamifyRepository {

    private val users = mutableListOf<User>(
        User(0, UserInfo("Kirill", 100)),
        User(1, UserInfo("Katya", 200)),
        User(2, UserInfo("Vitaliy", 300)),
        User(3, UserInfo("Alexey", 400)),
    )

    override fun getAllUsers(): List<User> {
        return users
    }

    override fun getUser(id: Int): User? {
        return users.firstOrNull { it.id == id }
    }

    override fun addUser(userInfo: UserInfo): User {
        val user = User(
            id = users.size + 1,
            userInfo = userInfo
        )
        users.add(user)
        return user
    }

    override fun removeUser(id: Int): Boolean {
        return users.removeIf { it.id == id }
    }

    override fun updateUser(id: Int, userInfo: UserInfo): Boolean {
        val user = users.firstOrNull { it.id == id }
            ?: return false

        user.userInfo.name = userInfo.name
        user.userInfo.points = userInfo.points
        return true
    }
}
