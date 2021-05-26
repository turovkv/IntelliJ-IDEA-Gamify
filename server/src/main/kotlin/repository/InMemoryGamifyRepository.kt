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

open class InMemoryGamifyRepository(private val storage: Storage = Storage()) : GamifyRepository {
    data class Storage(
        val digestFunction: (String) -> ByteArray = getDigestFunction("SHA-256") { "ktor${it.length}" },
        val usersHashedPasswords: MutableMap<String, ByteArray> = ConcurrentHashMap(),

        val users: MutableMap<String, User> = ConcurrentHashMap(),

        val lockOnAdd: ReentrantLock = ReentrantLock(),
        val usersLocks: MutableMap<String, ReadWriteLock> = ConcurrentHashMap(),
    )

    protected fun <T> withUserReadLock(name: String, action: () -> T): T {
        val lock = storage.usersLocks[name]?.readLock() ?: throw RepositoryException("No user with name $name")
        lock.withLock {
            return action()
        }
    }

    protected fun <T> withUserWriteLock(name: String, action: () -> T): T {
        val lock = storage.usersLocks[name]?.writeLock() ?: throw RepositoryException("No user with name $name")
        lock.withLock {
            return action()
        }
    }

    override fun getUserByName(name: String): User {
        return storage.users[name] ?: throw RepositoryException("No user with name $name")
    }

    override fun getUserInfoByName(name: String): UserInfo = withUserReadLock(name) {
        return@withUserReadLock getUserByName(name).userInfo
    }

    override fun getAllUserInfos(): List<UserInfo> {
        val list = arrayListOf<UserInfo>()
        for (user in storage.users.values) {
            list.add(user.userInfo)
        }
        return list
    }

    override fun createUser(credential: UserPasswordCredential) = storage.lockOnAdd.withLock {
        if (storage.users.contains(credential.name) || storage.usersHashedPasswords.contains(credential.name)) {
            throw RepositoryException("User with name ${credential.name} already exists")
        }
        storage.usersHashedPasswords[credential.name] = storage.digestFunction(credential.password)

        val user = User(
            name = credential.name
        )
        storage.users[user.name] = user
        storage.usersLocks[user.name] = ReentrantReadWriteLock()
    }

    override fun authenticate(credential: UserPasswordCredential): GamifyRepository.Authorized? {
        val userPasswordHash = storage.usersHashedPasswords[credential.name]
        if (userPasswordHash == null || !MessageDigest.isEqual(storage.digestFunction(credential.password), userPasswordHash)) {
            return null
        }

        return InMemoryGamifyRepositoryAuthorized(credential, storage)
    }
}

