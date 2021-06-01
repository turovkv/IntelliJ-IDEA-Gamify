package com.intellij.plugin.gamification.services.network

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@State(
    name = "ClientState",
    storages = [Storage("ClientState.xml")]
)
@Suppress("TooManyFunctions")
class NetworkService : PersistentStateComponent<NetworkService.ClientState>, Disposable {
    companion object {
        fun getInstance() = service<NetworkService>()
    }

    data class ClientState(
        var isSignedIn: Boolean,
        var user: User
    )

    private var state = ClientState(false, User("No name")) // ?
    override fun getState() = state
    override fun loadState(state: ClientState) {
        this.state = state
    }

    private val client: Client

    init {
        client = KtorClientImpl()
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
        state.user = User(name, state.user.userInfo)
        setPassword(state.user.name, password)
        client.signUp(name, password)
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

    suspend fun addNotification(notification: Notification): Unit =
        client.addNotification(notification)

    suspend fun subscribe(nameTo: String): Unit =
        client.subscribe(nameTo)

    suspend fun unsubscribe(nameFrom: String): Unit =
        client.unsubscribe(nameFrom)

    override fun dispose() {
        client.dispose()
    }
}

data class Notification(
    val text: String,
)
