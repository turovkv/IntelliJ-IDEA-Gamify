package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.*

interface GamifyRepository {
    fun getUserInfoById(id: Int): UserInfo
    fun getIdByName(name: String): Int
    fun getAllUserInfos(): List<UserInfo>

    fun addEmptyUser(credential: UserPasswordCredential): Int
    fun authenticate(credential: UserPasswordCredential): UserIdPrincipal?
    fun checkAccess(id: Int, name: String?)

    // authorised
    fun deleteUser(id: Int)
    fun updateUser(id: Int, userInfo: UserInfo)

    fun addNotification(id: Int, notification: Notification)
    fun subscribe(idFrom: Int, idTo: Int)
    fun unsubscribe(idFrom: Int, idTo: Int)
    fun getNotifications(id: Int): List<Notification>
}
