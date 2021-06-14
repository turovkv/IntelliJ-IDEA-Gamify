package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.NotificationHolder
import com.intellij.gamify.server.entities.Subscription
import com.intellij.gamify.server.entities.UserHolder
import com.intellij.gamify.server.entities.shared.User
import io.ktor.auth.UserPasswordCredential
import io.ktor.util.getDigestFunction
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import java.security.MessageDigest

open class MongoImplRepository(protected val storage: Storage = Storage()) : GamifyRepository {
    data class Storage(
        val client: CoroutineClient = KMongo.createClient().coroutine,
        val database: CoroutineDatabase = client.getDatabase("gamify"),

        val userHolders: CoroutineCollection<UserHolder> = database.getCollection(),
        val subscriptions: CoroutineCollection<Subscription> = database.getCollection(),
        val notificationHolders: CoroutineCollection<NotificationHolder> = database.getCollection(),

        val digestFunction: (String) -> ByteArray = getDigestFunction("SHA-256") { "ktor${it.length}" },
    )


    protected suspend fun getUserHolderByName(name: String): UserHolder {
        return storage.userHolders.findOne(UserHolder::user / User::name eq name) // is it too slow ?
            ?: throw RepositoryException("No user with name $name")
    }

    override suspend fun getUserByName(name: String): User {
        return getUserHolderByName(name).user
    }

    override suspend fun getAllUsers(): List<User> {
        return storage.userHolders.find().toList().map { it.user }
    }

    override suspend fun createUser(credential: UserPasswordCredential) {
        if (storage.userHolders.countDocuments(UserHolder::user / User::name eq credential.name) != 0L) {
            throw RepositoryException("User with name ${credential.name} already exists")
        }
        if (!storage.userHolders.insertOne(
                UserHolder(
                    user = User(credential.name),
                    passwordHash = storage.digestFunction(credential.password),
                )
            ).wasAcknowledged()
        ) {
            throw RepositoryException("Unable to create user ${credential.name}")
        }
    }

    override suspend fun authenticate(credential: UserPasswordCredential): GamifyRepository.Authorized? {
        if (!MessageDigest.isEqual(
                getUserHolderByName(credential.name).passwordHash,
                storage.digestFunction(credential.password)
            )
        ) {
            return null
        }
        return MongoImplRepositoryAuthorized(credential, storage)
    }
}
