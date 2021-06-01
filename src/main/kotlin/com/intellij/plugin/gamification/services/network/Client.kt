package com.intellij.plugin.gamification.services.network

import com.intellij.openapi.Disposable

interface Client : Disposable {
    suspend fun getUserByName(name: String): User
    suspend fun getAllUsers(): List<User>

    suspend fun signUp(name: String, password: String)
    fun signIn(newName: String, newPassword: String)

    suspend fun updateUserInfo(userInfo: UserInfo)

    suspend fun addNotification(notification: Notification)
    suspend fun subscribe(nameTo: String)
    suspend fun unsubscribe(nameFrom: String)
    suspend fun getNotifications(): List<Notification>
}
