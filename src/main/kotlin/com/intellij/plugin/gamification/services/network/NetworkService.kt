package com.intellij.plugin.gamification.services.network

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@State(
    name = "ClientState",
    storages = [Storage("ClientState.xml")]
)
@Suppress("TooManyFunctions")
class NetworkService : PersistentStateComponent<NetworkService.ClientState>, Disposable {
    companion object {
        fun getInstance() = service<NetworkService>()
        val NOTIFICATION_GROUP =
            NotificationGroup(
                "Gamify",
                NotificationDisplayType.BALLOON,
                true
            )
        const val requestDelay: Long = 1000
    }

    class ClientState {
        var isSignedIn: Boolean = false
        var user: User = User()
    }

    private var state = ClientState()
    override fun getState() = state
    override fun loadState(state: ClientState) {
        this.state = state
    }

    private val client: Client = KtorClientImpl()

    override fun initializeComponent() {
        if (state.isSignedIn) {
            client.signIn(
                state.user.name,
                getPassword(state.user.name)!!
            )
        }
    }

    private fun setPassword(name: String, password: String) {
        PasswordSafe
            .instance
            .set(
                CredentialAttributes(NetworkService::class.java.name, name),
                Credentials(name, password)
            )
    }

    private fun getPassword(name: String): String? {
        return PasswordSafe
            .instance
            .getPassword(
                CredentialAttributes(NetworkService::class.java.name, name)
            )
    }

    suspend fun signUp(name: String, password: String) {
        client.signUp(name, password)
        state.user = User(name, state.user.userInfo)
        setPassword(state.user.name, password)
        client.updateUserInfo(state.user.userInfo)
        state.isSignedIn = true
    }

    suspend fun updateUserInfo(userInfo: UserInfo) {
        client.updateUserInfo(userInfo)
        state.user.userInfo = userInfo
    }

    suspend fun getAllUsers(): List<User> =
        client.getAllUsers()

    suspend fun getNotifications(): List<Notification> =
        client.getNotifications()

    fun addNotification(notification: Notification) {
        // save it if connection failed ?
        GlobalScope.launch {
            try {
                client.addNotification(
                    notification
                )
            } catch (e: ClientException) {
                Logger
                    .getFactory()
                    .getLoggerInstance("Gamify")
                    .error(e)
                println(e.localizedMessage)
            }
        }
    }

    fun createNotificationByText(text: String): Notification {
        return Notification("${state.user.name}: $text")
    }

    fun addNotificationByText(text: String) {
        addNotification(createNotificationByText(text))
    }

    suspend fun subscribe(nameTo: String): Unit =
        client.subscribe(nameTo)

    suspend fun unsubscribe(nameFrom: String): Unit =
        client.unsubscribe(nameFrom)

    fun launchNotificationReceiver() {
        GlobalScope.launch {
            while (true) {
                try {
                    if (!state.isSignedIn) {
                        continue
                    }
                    getNotifications().map {
                        NOTIFICATION_GROUP
                            .createNotification(it.text, NotificationType.INFORMATION)
                            .notify(null)
                    }
                } catch (e: ClientException) {
                    Logger
                        .getFactory()
                        .getLoggerInstance("Gamify")
                        .error(e)
                    println(e.localizedMessage)
                }
                // what if there is no connection to the internet? :(
                delay(requestDelay)
            }
        }
    }

    override fun dispose() {
        client.dispose()
    }
}

data class Notification(
    val text: String,
)
