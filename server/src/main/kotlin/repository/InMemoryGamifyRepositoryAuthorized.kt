package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.UserHolder
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.UserPasswordCredential

class InMemoryGamifyRepositoryAuthorized(
    credential: UserPasswordCredential,
    storage: Storage
) : GamifyRepository.Authorized, InMemoryGamifyRepository(storage) {

    override val userPrincipal: UserIdPrincipal = UserIdPrincipal(credential.name)
    private val userHolder: UserHolder = getUserHolderByName(userPrincipal.name)

    override fun updateUserInfo(userInfo: UserInfo) {
        userHolder.updateUserInfo(userInfo)
    }

    override fun addNotification(notification: Notification) {
        userHolder.addNotification(notification)
    }

    override fun subscribe(nameTo: String) {
        userHolder.subscribe(getUserHolderByName(nameTo))
    }

    override fun unsubscribe(nameFrom: String) {
        userHolder.unsubscribe(getUserHolderByName(nameFrom))
    }

    override fun getNotifications(): List<Notification> {
        return userHolder.collectSubscribingNotifications()
    }
}
