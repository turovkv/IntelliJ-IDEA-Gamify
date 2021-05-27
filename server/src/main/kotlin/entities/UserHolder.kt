package com.intellij.gamify.server.entities

import io.ktor.auth.UserPasswordCredential
import io.ktor.util.getDigestFunction
import java.security.MessageDigest
import java.sql.Timestamp
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

class UserHolder(credential: UserPasswordCredential) {
    val user: User = User(credential.name)

    private var lastWatched: Timestamp = Timestamp(System.currentTimeMillis())
    private val notifications: MutableList<NotificationWithTime> = CopyOnWriteArrayList()
    private val subscribing: MutableSet<UserHolder> = CopyOnWriteArraySet()

    private val digestFunction: (String) -> ByteArray = getDigestFunction("SHA-256") { "ktor${it.length}" }
    private var passwordHash: ByteArray = digestFunction(credential.password)

    fun updateUserInfo(userInfo: UserInfo) {
        user.userInfo = userInfo
    }

    fun authenticate(password: String): Boolean {
        return MessageDigest.isEqual(passwordHash, digestFunction(password))
    }

    fun addNotification(notification: Notification) {
        notifications.add(NotificationWithTime(notification))
        if (notifications.size > 50) {
            notifications.removeAt(0)
        }
    }

    fun subscribe(userHolder: UserHolder) {
        subscribing.add(userHolder)
    }

    fun unsubscribe(userHolder: UserHolder) {
        subscribing.remove(userHolder)
    }

    private fun getNotificationsFromTime(timestamp: Timestamp): List<NotificationWithTime> {
        return notifications
            .asSequence()
            .filter { it.serverTime.after(timestamp) }
            .toList()
    }

    fun collectSubscribingNotifications(): List<Notification> {
        val list: MutableList<NotificationWithTime> = ArrayList()
        for (celeb in subscribing) {
            list.addAll(celeb.getNotificationsFromTime(lastWatched))
        }
        lastWatched = Timestamp(System.currentTimeMillis())
        return list
            .asSequence()
            .sortedBy { it.serverTime }
            .map { it.notification }
            .toList()
    }
}
