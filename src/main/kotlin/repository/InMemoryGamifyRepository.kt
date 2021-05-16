package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.NotificationWithTime
import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo
import java.sql.Timestamp
import java.util.concurrent.ConcurrentHashMap

class InMemoryGamifyRepository : GamifyRepository {

    private val users = ConcurrentHashMap(
        mapOf(
            0 to User(0, UserInfo("Kirill", "Kirill", 1)),
            1 to User(1, UserInfo("Katya", "Katya", 2)),
            2 to User(2, UserInfo("Vitaliy", "Vitaliy", 3)),
            3 to User(3, UserInfo("Alexey", "Alexey", 4)),
        )
    )
    private var nextUserId = users.size

    private val nameToId = ConcurrentHashMap(
        mapOf(
            "Kirill" to 0,
            "Katya" to 1,
            "Vitaliy" to 2,
            "Alexey" to 3,
        )
    )

    override fun getAllUserInfos(): List<UserInfo> {
        return users.values.map { it.userInfo }
    }

    override fun getUserById(id: Int): User {
        return users[id] ?: throw RepositoryException("No user with id $id")
    }

    override fun getIdByName(name: String): Int {
        return nameToId[name]
            ?: throw RepositoryException("No user with name $name")
    }

    override fun addUser(userInfo: UserInfo): Int {
        val user = User(
            id = nextUserId,
            userInfo = userInfo
        )

        if (nameToId.contains(userInfo.name)) {
            throw RepositoryException("User with name ${userInfo.name} already exists")
        }

        nameToId[userInfo.name] = nextUserId
        users[nextUserId] = user

        nextUserId += 1
        return user.id
    }

    override fun deleteUser(id: Int) {
        val user = getUserById(id)
        nameToId.remove(user.userInfo.name)
        users.remove(id)
    }

    override fun updateUser(id: Int, userInfo: UserInfo) {
        val user = getUserById(id)
        nameToId.remove(user.userInfo.name)
        user.userInfo = userInfo
        nameToId[user.userInfo.name] = user.id
    } // concurrency ((((

    override fun addEvent(id: Int, notification: Notification) {
        val user = getUserById(id)
        val notificationWithTime = NotificationWithTime(
            notification,
            Timestamp(System.currentTimeMillis())
        )

        user.notifications.addLast(notificationWithTime)
    }

    override fun subscribe(idFrom: Int, idTo: Int) {
        val userFrom = getUserById(idFrom)
        if (!userFrom.subscribing.add(idTo)) {
            throw RepositoryException("UserId $idFrom already subscribed to $idTo")
        }
    }

    override fun unsubscribe(idFrom: Int, idTo: Int) {
        val userFrom = getUserById(idFrom)
        if (!userFrom.subscribing.remove(idTo)) {
            throw RepositoryException("UserId $idFrom not subscribed to $idTo")
        }
    }

    override fun getNotifications(id: Int): List<Notification> {
        val user = getUserById(id)
        val list = ArrayList<NotificationWithTime>()
        for (celebId in user.subscribing) {
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
        }

        user.lastWatched = Timestamp(System.currentTimeMillis())

        return list
            .asSequence()
            .sortedBy { it.serverTime }
            .map { it.notification }
            .toList()
    }
}
