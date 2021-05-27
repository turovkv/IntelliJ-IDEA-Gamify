package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.NotificationWithTime
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.UserPasswordCredential
import java.sql.Timestamp

class InMemoryGamifyRepositoryAuthorized(
    credential: UserPasswordCredential,
    storage: Storage
) : GamifyRepository.Authorized, InMemoryGamifyRepository(storage) {
    
    override val userPrincipal: UserIdPrincipal = UserIdPrincipal(credential.name)
    private val myName: String = userPrincipal.name

    override fun updateUser(userInfo: UserInfo): Unit = withUserWriteLock(myName) {
        val user = getUserByName(myName)
        user.userInfo = userInfo
    }

    override fun addNotification(notification: Notification): Unit = withUserWriteLock(myName) {
        val user = getUserByName(myName)
        val notificationWithTime = NotificationWithTime(
            notification,
            Timestamp(System.currentTimeMillis())
        )

        user.notifications.addLast(notificationWithTime)
    }

    override fun subscribe(nameTo: String): Unit = withUserWriteLock(myName) {
        val userFrom = getUserByName(myName)
        if (!userFrom.subscribing.add(nameTo)) {
            throw RepositoryException("UserId $myName already subscribed to $nameTo")
        }
    }

    override fun unsubscribe(nameFrom: String): Unit = withUserWriteLock(myName) {
        val userFrom = getUserByName(myName)
        if (!userFrom.subscribing.remove(nameFrom)) {
            throw RepositoryException("UserId $myName not subscribed to $nameFrom")
        }
    }

    override fun getNotifications(): List<Notification> = withUserReadLock(myName) {
        val user = getUserByName(userPrincipal.name)
        val list = ArrayList<NotificationWithTime>()
        for (celebId in user.subscribing) {
            withUserReadLock(celebId) {
                val celeb = getUserByName(celebId)
                val it = celeb.notifications.descendingIterator()
                while (it.hasNext()) {
                    val nWithTime = it.next()
                    if (nWithTime.serverTime.after(user.lastWatched)) {
                        list.add(nWithTime)
                    } else {
                        break
                    }
                }
            } // exception?
        }

        user.lastWatched = Timestamp(System.currentTimeMillis())

        return@withUserReadLock list
            .asSequence()
            .sortedBy { it.serverTime }
            .map { it.notification }
            .toList()
    }
}