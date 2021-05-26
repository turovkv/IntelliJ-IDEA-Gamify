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
    
    // TODO: avoid using numeric id
    override fun updateUser(userInfo: UserInfo): Unit = withUserWriteLock(getIdByName(userPrincipal.name)) {
        val user = getUserById(getIdByName(userPrincipal.name))
        user.userInfo = userInfo
    }

    override fun addNotification(id: Int, notification: Notification): Unit = withUserWriteLock(id) {
        val user = getUserById(id)
        val notificationWithTime = NotificationWithTime(
            notification,
            Timestamp(System.currentTimeMillis())
        )

        user.notifications.addLast(notificationWithTime)
    }

    override fun subscribe(idFrom: Int, idTo: Int): Unit = withUserWriteLock(idFrom) {
        val userFrom = getUserById(idFrom)
        if (!userFrom.subscribing.add(idTo)) {
            throw RepositoryException("UserId $idFrom already subscribed to $idTo")
        }
    }

    override fun unsubscribe(idFrom: Int, idTo: Int): Unit = withUserWriteLock(idFrom) {
        val userFrom = getUserById(idFrom)
        if (!userFrom.subscribing.remove(idTo)) {
            throw RepositoryException("UserId $idFrom not subscribed to $idTo")
        }
    }

    override fun getNotifications(id: Int): List<Notification> = withUserReadLock(id) {
        val user = getUserById(id)
        val list = ArrayList<NotificationWithTime>()
        for (celebId in user.subscribing) {
            withUserReadLock(celebId) {
                val celeb = getUserById(celebId)
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