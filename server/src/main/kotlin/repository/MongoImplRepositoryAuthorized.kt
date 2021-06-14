package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.NotificationHolder
import com.intellij.gamify.server.entities.Subscription
import com.intellij.gamify.server.entities.UserHolder
import com.intellij.gamify.server.entities.shared.Notification
import com.intellij.gamify.server.entities.shared.User
import com.intellij.gamify.server.entities.shared.UserInfo
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.UserPasswordCredential
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.and
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.setValue
import java.sql.Timestamp

class MongoImplRepositoryAuthorized(
    credential: UserPasswordCredential,
    storage: Storage
) : GamifyRepository.Authorized, MongoImplRepository(storage) {

    override val userPrincipal: UserIdPrincipal = UserIdPrincipal(credential.name)
    private val userHolder: UserHolder = runBlocking { getUserHolderByName(credential.name) }
    private val userName: String = userHolder.user.name

    override suspend fun updateUserInfo(userInfo: UserInfo) {
        val updateResult = storage.userHolders.updateOne(
            UserHolder::user / User::name eq userName,
            setValue(UserHolder::user / User::userInfo, userInfo)
        )
        if (!updateResult.wasAcknowledged()) {
            throw RepositoryException("Unable to update $userName")
        }
    }

    override suspend fun addNotification(notification: Notification) {
        storage.notificationHolders.insertOne(NotificationHolder(notification, userName))
    }

    override suspend fun subscribe(nameTo: String) {
        val subscription = Subscription(userName, nameTo)
        if (storage.subscriptions.countDocuments(
                and(
                    Subscription::from eq subscription.from,
                    Subscription::to eq subscription.to
                )
            ) == 0L
        ) {
            storage.subscriptions.insertOne(subscription)
        }
    }

    override suspend fun unsubscribe(nameTo: String) {
        val subscription = Subscription(userName, nameTo)
        val subscriptionCondition = and(
            Subscription::from eq subscription.from,
            Subscription::to eq subscription.to
        )

        if (storage.subscriptions.countDocuments(subscriptionCondition) != 0L) {
            storage.subscriptions.deleteOne(subscriptionCondition)
        }
    }

    override suspend fun getNotifications(): List<Notification> {
        val list: MutableList<NotificationHolder> = arrayListOf()
        for (userNameTo in storage.subscriptions.find(Subscription::from eq userName).toList().map { it.to }) {
            list.addAll(
                storage.notificationHolders.find(
                    NotificationHolder::author eq userNameTo,
                    NotificationHolder::serverTime gt userHolder.lastWatched
                ).toList()
            )
        }
        list.sortBy { it.serverTime }
        storage.userHolders.updateOne(
            UserHolder::user / User::name eq userName,
            setValue(UserHolder::lastWatched, Timestamp(System.currentTimeMillis()))
        )
        return list.map { it.notification }
    }
}
