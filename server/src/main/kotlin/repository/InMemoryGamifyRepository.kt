package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserInfo
import io.ktor.auth.UserPasswordCredential
import io.ktor.util.getDigestFunction
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

open class InMemoryGamifyRepository(protected val storage: Storage = Storage()) : GamifyRepository {
    data class Storage(
        val digestFunction: (String) -> ByteArray = getDigestFunction("SHA-256") { "ktor${it.length}" },
        val usersHashedPasswords: MutableMap<String, ByteArray> = ConcurrentHashMap(),

        val users: MutableMap<Int, User> = ConcurrentHashMap(),
        var nextUserId: Int = users.size,

        val nameToId: MutableMap<String, Int> = ConcurrentHashMap(),

        val lockOnAdd: ReentrantLock = ReentrantLock(),
        val usersLocks: MutableMap<Int, ReadWriteLock> = ConcurrentHashMap(),
    )

    fun <T> withUserReadLock(id: Int, action: () -> T): T {
        val lock = storage.usersLocks[id]?.readLock() ?: throw RepositoryException("No user with id $id")
        lock.withLock {
            return action()
        }
    }

    fun <T> withUserWriteLock(id: Int, action: () -> T): T {
        val lock = storage.usersLocks[id]?.writeLock() ?: throw RepositoryException("No user with id $id")
        lock.withLock {
            return action()
        }
    }

    fun getUserById(id: Int): User {
        return storage.users[id] ?: throw RepositoryException("No user with id $id")
    }

    override fun getUserInfoById(id: Int): UserInfo = withUserReadLock(id) {
        return@withUserReadLock getUserById(id).userInfo
    }

    override fun getAllUserInfos(): List<UserInfo> {
        val list = arrayListOf<UserInfo>()
        for (id in 0 until storage.nextUserId) {
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
        return storage.nameToId[name] ?: throw RepositoryException("No user with name $name")
    }

    override fun createUser(credential: UserPasswordCredential): Int = storage.lockOnAdd.withLock {
        if (storage.nameToId.contains(credential.name) || storage.usersHashedPasswords.contains(credential.name)) {
            throw RepositoryException("User with name ${credential.name} already exists")
        }
        storage.usersHashedPasswords[credential.name] = storage.digestFunction(credential.password)

        val user = User(
            id = storage.nextUserId,
            name = credential.name
        )
        storage.users[user.id] = user
        storage.nameToId[user.name] = user.id
        storage.usersLocks[user.id] = ReentrantReadWriteLock()

        storage.nextUserId += 1
        return user.id
    }

    override fun authenticate(credential: UserPasswordCredential): GamifyRepository.Authorized? {
        val userPasswordHash = storage.usersHashedPasswords[credential.name]
        if (userPasswordHash == null || !MessageDigest.isEqual(storage.digestFunction(credential.password), userPasswordHash)) {
            return null
        }

        return InMemoryGamifyRepositoryAuthorized(credential, storage)
    }
}

