package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserHolder
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.*

interface GamifyRepository {
    fun getUserByName(name: String): User
    fun getAllUsers(): List<User>

    fun createUser(credential: UserPasswordCredential)
    fun authenticate(credential: UserPasswordCredential): Authorized?

    interface Authorized : GamifyRepository {
        val userPrincipal: UserIdPrincipal

        fun updateUserInfo(userInfo: UserInfo)

        fun addNotification(notification: Notification)
        fun subscribe(nameTo: String)
        fun unsubscribe(nameFrom: String)
        fun getNotifications(): List<Notification>
    }
}
