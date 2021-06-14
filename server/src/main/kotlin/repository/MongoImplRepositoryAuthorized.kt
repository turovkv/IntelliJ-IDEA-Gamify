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

class MongoImplRepositoryAuthorized(
    credential: UserPasswordCredential,
    storage: Storage
) : GamifyRepository.Authorized, MongoImplRepository(storage) {

    override val userPrincipal: UserIdPrincipal = UserIdPrincipal(credential.name)
    private val userHolder: UserHolder = runBlocking { getUserHolderByName(credential.name) }
    private val currentUserName: String = userHolder.user.name

    override suspend fun updateUserInfo(userInfo: UserInfo) {
        val updateResult = storage.userHolders.updateOne(
            UserHolder::user / User::name eq currentUserName,
            setValue(UserHolder::user / User::userInfo, userInfo)
        )
        if (!updateResult.wasAcknowledged()) {
            throw RepositoryException("Unable to update from user $currentUserName")
        }
    }

    override suspend fun addNotification(notification: Notification) {
        val insertResult = storage.notificationHolders.insertOne(NotificationHolder(notification, currentUserName))
        if (!insertResult.wasAcknowledged()) {
            throw RepositoryException("Unable to add notification from user $currentUserName")
        }
    }

    override suspend fun subscribe(nameTo: String) {
        val subscription = Subscription(currentUserName, nameTo)
        if (storage.subscriptions.countDocuments(
                and(
                    Subscription::from eq subscription.from,
                    Subscription::to eq subscription.to
                )
            ) == 0L
        ) {
            storage.subscriptions.insertOne(subscription)
        }
        //else ??
    }

    override suspend fun unsubscribe(nameTo: String) {
        val subscription = Subscription(currentUserName, nameTo)
        val subscriptionQuery = and(
            Subscription::from eq subscription.from,
            Subscription::to eq subscription.to
        )

        if (storage.subscriptions.countDocuments(subscriptionQuery) != 0L) {
            storage.subscriptions.deleteOne(subscriptionQuery)
        }
        //else ??
    }

    override suspend fun getNotifications(): List<Notification> {
        val list: MutableList<NotificationHolder> = arrayListOf()
        val namesSubscribedTo = storage
            .subscriptions
            .find(Subscription::from eq currentUserName)
            .toList()
            .map { it.to }
        for (userNameTo in namesSubscribedTo) {
            list.addAll(
                storage.notificationHolders.find(
                    NotificationHolder::author eq userNameTo,
                    NotificationHolder::serverTime gt userHolder.lastWatched
                ).toList()
            )
        }
        list.sortBy { it.serverTime }
        storage.userHolders.updateOne(
            UserHolder::user / User::name eq currentUserName,
            setValue(UserHolder::lastWatched, System.currentTimeMillis())
        )
        return list.map { it.notification }
    }
}
