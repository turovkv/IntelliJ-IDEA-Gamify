package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.*

interface GamifyRepository {
    fun getUserInfoByName(name: String): UserInfo
    fun getAllUserInfos(): List<UserInfo>
    fun getUserByName(name: String): User

    fun createUser(credential: UserPasswordCredential)
    fun authenticate(credential: UserPasswordCredential): Authorized?

    interface Authorized : GamifyRepository {
        val userPrincipal: UserIdPrincipal

        fun updateUser(userInfo: UserInfo)

        fun addNotification(notification: Notification)
        fun subscribe(nameTo: String)
        fun unsubscribe(nameTo: String)
        fun getNotifications(): List<Notification>
    }
}
