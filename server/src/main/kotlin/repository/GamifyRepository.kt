package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.shared.Notification
import com.intellij.gamify.server.entities.shared.User
import com.intellij.gamify.server.entities.shared.UserInfo
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.UserPasswordCredential

interface GamifyRepository {
    suspend fun getUserByName(name: String): User
    suspend fun getAllUsers(): List<User>

    suspend fun createUser(credential: UserPasswordCredential)
    suspend fun authenticate(credential: UserPasswordCredential): Authorized?

    interface Authorized : GamifyRepository {
        val userPrincipal: UserIdPrincipal

        suspend fun updateUserInfo(userInfo: UserInfo)

        suspend fun addNotification(notification: Notification)
        suspend fun subscribe(nameTo: String)
        suspend fun unsubscribe(nameTo: String)
        suspend fun getNotifications(): List<Notification>
    }
}
