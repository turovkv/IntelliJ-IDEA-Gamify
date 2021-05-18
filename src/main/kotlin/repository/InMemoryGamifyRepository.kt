package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.NotificationWithTime
import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.*
import io.ktor.util.*
import java.security.MessageDigest
import java.sql.Timestamp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class InMemoryGamifyRepository : GamifyRepository {
    private val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }
    private val hashedPaswords: MutableMap<String, ByteArray> = ConcurrentHashMap(
        mapOf(
            "0" to digestFunction("0"),
            "1" to digestFunction("1"),
            "2" to digestFunction("2"),
            "3" to digestFunction("3"),
        )
    )

    private val users: MutableMap<Int, User> = ConcurrentHashMap(
        mapOf(
            0 to User(0, "Kirill"),
            1 to User(1, "Katya"),
            2 to User(2, "Vitaliy"),
            3 to User(3, "Alexey"),
        )
    )
    private var nextUserId = users.size

    private val nameToId: MutableMap<String, Int> = ConcurrentHashMap(
        mapOf(
            "Kirill" to 0,
            "Katya" to 1,
            "Vitaliy" to 2,
            "Alexey" to 3,
        )
    )

    private val lockOnAdd = ReentrantLock()
    private val usersLocks: MutableMap<Int, ReadWriteLock> = ConcurrentHashMap(
        mapOf(
            0 to ReentrantReadWriteLock(),
            1 to ReentrantReadWriteLock(),
            2 to ReentrantReadWriteLock(),
            3 to ReentrantReadWriteLock(),
        )
    )

    private fun <T> withOnAddLock(action: () -> T): T {
        lockOnAdd.lock()
        try {
            return action()
        } finally {
            lockOnAdd.unlock()
        }
    }

    private fun <T> withUserReadLock(id: Int, action: () -> T): T {
        val lock = usersLocks[id]?.readLock() ?: throw RepositoryException("No user with id $id")
        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }

    private fun <T> withUserWriteLock(id: Int, action: () -> T): T {
        val lock = usersLocks[id]?.writeLock() ?: throw RepositoryException("No user with id $id")
        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }

    private fun getUserById(id: Int): User {
        return users[id] ?: throw RepositoryException("No user with id $id")
    }


    override fun getUserInfoById(id: Int): UserInfo = withUserReadLock(id) {
        return@withUserReadLock getUserById(id).userInfo
    }

    override fun getAllUserInfos(): List<UserInfo> {
        val list = arrayListOf<UserInfo>()
        for (id in 0 until nextUserId) {
            try {
                withUserReadLock(id) {
                    list.add(getUserById(id).userInfo)
                }
            } catch (e: RepositoryException) {
            }
        }
        return list
    }

    override fun getIdByName(name: String): Int {
        return nameToId[name] ?: throw RepositoryException("No user with name $name")
    }

    override fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
        val userPasswordHash = hashedPaswords[credential.name]
        if (userPasswordHash != null && MessageDigest.isEqual(digestFunction(credential.password), userPasswordHash)) {
            return UserIdPrincipal(credential.name)
        }

        return null
    }

    override fun checkAccess(id: Int, name: String?) {
        if (name == null) {
            throw RepositoryException("Try access with no name")
        }
        if (id != getIdByName(name)) {
            throw RepositoryException("Illegal access")
        }
    }

    override fun addEmptyUser(credential: UserPasswordCredential): Int = withOnAddLock {
        if (nameToId.contains(credential.name) || hashedPaswords.contains(credential.name)) {
            throw RepositoryException("User with name ${credential.name} already exists")
        }
        hashedPaswords[credential.name] = digestFunction(credential.password)

        val user = User(
            id = nextUserId,
            name = credential.name
        )
        users[user.id] = user
        nameToId[user.name] = user.id
        usersLocks[user.id] = ReentrantReadWriteLock()

        nextUserId += 1
        return@withOnAddLock user.id
    }

    override fun deleteUser(id: Int): Unit = withUserWriteLock(id) {
        val user = getUserById(id)
        usersLocks.remove(id)
        users.remove(id)
        nameToId.remove(user.name)
    }

    override fun updateUser(id: Int, userInfo: UserInfo): Unit = withUserWriteLock(id) {
        val user = getUserById(id)
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
