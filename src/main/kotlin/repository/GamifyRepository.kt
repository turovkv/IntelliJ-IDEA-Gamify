package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo

interface GamifyRepository {
    fun addUser(userInfo: UserInfo): Int
    fun getAllUserInfos(): List<UserInfo>
    fun getIdByName(name: String): Int
    fun getUserById(id: Int): User

    // authorised
    fun deleteUser(id: Int)
    fun updateUser(id: Int, userInfo: UserInfo)

    fun addEvent(id: Int, notification: Notification)
    fun subscribe(idFrom: Int, idTo: Int)
    fun unsubscribe(idFrom: Int, idTo: Int)
    fun getNotifications(id: Int): List<Notification>
}
