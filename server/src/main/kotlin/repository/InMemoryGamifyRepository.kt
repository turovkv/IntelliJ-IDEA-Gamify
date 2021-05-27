package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.User
import com.intellij.gamify.server.entities.UserHolder
import io.ktor.auth.UserPasswordCredential
import java.util.concurrent.ConcurrentHashMap

open class InMemoryGamifyRepository(private val storage: Storage = Storage()) : GamifyRepository {
    data class Storage(
        val userHolders: MutableMap<String, UserHolder> = ConcurrentHashMap()
    )

    protected fun getUserHolderByName(name: String): UserHolder {
        return storage.userHolders[name]
            ?: throw RepositoryException("No user with name $name")
    }

    override fun getUserByName(name: String): User {
        return getUserHolderByName(name).user
    }

    override fun getAllUsers(): List<User> {
        return storage.userHolders.values.map { it.user }
    }

    override fun createUser(credential: UserPasswordCredential) {
        if (storage.userHolders.contains(credential.name)) {
            throw RepositoryException("User with name ${credential.name} already exists")
        }
        storage.userHolders[credential.name] = UserHolder(credential)
    }

    override fun authenticate(credential: UserPasswordCredential): GamifyRepository.Authorized? {
        if (!getUserHolderByName(credential.name).authenticate(credential.password)) {
            return null
        }
        return InMemoryGamifyRepositoryAuthorized(credential, storage)
    }
}
