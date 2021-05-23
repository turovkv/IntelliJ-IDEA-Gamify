package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.entities.NotificationWithTime
import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.UserPasswordCredential
import io.ktor.util.getDigestFunction
import java.security.MessageDigest
import java.sql.Timestamp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class InMemoryGamifyRepository : GamifyRepository {
    private val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }
    private val usersHashedPasswords: MutableMap<String, ByteArray> = ConcurrentHashMap()

    private val users: MutableMap<Int, User> = ConcurrentHashMap()
    private var nextUserId = users.size

    private val nameToId: MutableMap<String, Int> = ConcurrentHashMap()

    private val lockOnAdd = ReentrantLock()
    private val usersLocks: MutableMap<Int, ReadWriteLock> = ConcurrentHashMap()

    private fun <T> withUserReadLock(id: Int, action: () -> T): T {
        val lock = usersLocks[id]?.readLock() ?: throw RepositoryException("No user with id $id")
        lock.withLock {
            return action()
        }
    }

    private fun <T> withUserWriteLock(id: Int, action: () -> T): T {
        val lock = usersLocks[id]?.writeLock() ?: throw RepositoryException("No user with id $id")
        lock.withLock {
            return action()
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
        val userPasswordHash = usersHashedPasswords[credential.name]
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

    override fun createUser(credential: UserPasswordCredential): Int = lockOnAdd.withLock {
        if (nameToId.contains(credential.name) || usersHashedPasswords.contains(credential.name)) {
            throw RepositoryException("User with name ${credential.name} already exists")
        }
        usersHashedPasswords[credential.name] = digestFunction(credential.password)

        val user = User(
            id = nextUserId,
            name = credential.name
        )
        users[user.id] = user
        nameToId[user.name] = user.id
        usersLocks[user.id] = ReentrantReadWriteLock()

        nextUserId += 1
        return user.id
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
