package com.intellij.plugin.gamification.services

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import io.ktor.auth.UserPasswordCredential
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpRequestTimeoutException
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.ResponseException
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.feature
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.contentType

@State(
    name = "ClientState",
    storages = [Storage("ClientState.xml")]
)
@Suppress("TooManyFunctions")
class NetworkService : PersistentStateComponent<NetworkService.ClientState>, Disposable {
    companion object {
        fun getInstance() = service<NetworkService>()

        private const val requestTimeoutMillisConf: Long = 1000
        private const val connectTimeoutMillisConf: Long = 1000
        private const val serverUrl = "http://0.0.0.0:8080"
    }

    class ClientState {
        var isSignedIn: Boolean = false
        var userId: Int = -1
        var login: String = "No login"
        var userInfo: UserInfo = UserInfo()
    }

    private var state = ClientState()
    override fun getState() = state
    override fun loadState(state: ClientState) {
        this.state = state
    }

    fun getMyId() = state.userId
    fun getMyLogin() = state.login
    fun getMyUserInfo() = state.userInfo
    fun isSignedIn() = state.isSignedIn

    private val url = serverUrl
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeoutMillisConf
            connectTimeoutMillis = connectTimeoutMillisConf // когда может случиться?
        }
        install(Auth) {
            basic {
                username = state.login
                password = getPassword(state.login) ?: "No password" // is it ok ???
            }
        }
    }

    private fun changeClientCredentials(newUser: String, newPassword: String) {
        val auth = client.feature(Auth)
        if (auth != null) {
            auth.providers.removeAt(0)
            auth.basic {
                username = newUser
                password = newPassword
            }
        } else {
            throw NetworkServiceException("Auth feature not installed on HttpClient")
        }
    }

    private fun setPassword(user: String, password: String) {
        PasswordSafe
            .instance
            .set(
                CredentialAttributes(NetworkService::class.java.name, user),
                Credentials(user, password)
            )
    }

    private fun getPassword(user: String): String? {
        return PasswordSafe
            .instance
            .getPassword(
                CredentialAttributes(NetworkService::class.java.name, user)
            ) // what if null ? TODO
    }

    suspend fun signUp(user: String, password: String) {
        val newId: Int = carefulRequest("signUp") {
            client.post("$url/users") {
                contentType(ContentType.Application.Json)
                body = UserPasswordCredential(
                    user,
                    password
                )
            }
        }

        state.isSignedIn = true
        state.userId = newId
        state.login = user
        setPassword(user, password)
        changeClientCredentials(user, password)
    }

    suspend fun updateUserInfo(userInfo: UserInfo) {
        carefulRequest<HttpStatement>("updateUserInfo", true) {
            client.put("$url/users/${state.userId}") {
                contentType(ContentType.Application.Json)
                body = userInfo
            }
        }
        state.userInfo = userInfo
    }

    suspend fun getUsersInfos(): List<UserInfo> =
        carefulRequest("getUsersInfos") {
            client.get("$url/users")
        }

    suspend fun getNotifications(): List<Notification> =
        carefulRequest("getNotifications", true) {
            client.get("$url/users/notifications/${state.userId}")
        }

    suspend fun addNotification(notification: Notification): Unit =
        carefulRequest("addNotification", true) {
            client.post("$url/users/notifications/${state.userId}") {
                contentType(ContentType.Application.Json)
                body = notification
            }
        }

    suspend fun subscribe(idTo: Int): Unit =
        carefulRequest("subscribe", true) {
            client.post("$url/users/subscribing/${state.userId}") {
                contentType(ContentType.Application.Json)
                body = idTo
            }
        }

    suspend fun unsubscribe(idTo: Int): Unit =
        carefulRequest("unsubscribe", true) {
            client.delete("$url/users/subscribing/${state.userId}") {
                contentType(ContentType.Application.Json)
                body = idTo
            }
        }

    private suspend fun <T> carefulRequest(
        requestName: String,
        signedIn: Boolean = false,
        request: suspend () -> T
    ): T {
        if (signedIn && !state.isSignedIn) {
            throw NetworkServiceException("Failed to $requestName (you are not signed in)")
        }
        try {
            return request()
        } catch (e: HttpRequestTimeoutException) {
            throw NetworkServiceException("Failed to $requestName (server did not respond)", e)
        } catch (e: NoTransformationFoundException) { // not sure
            throw NetworkServiceException("Failed to $requestName (invalid data)", e)
        } catch (e: ResponseException) {
            try {
                val errorMessage: String = e.response.readText()
                if (errorMessage.isNotEmpty()) {
                    throw NetworkServiceException("Failed to $requestName ($errorMessage)")
                } else {
                    throw IllegalStateException("")
                }
            } catch (ignored: IllegalStateException) {
                throw NetworkServiceException("Failed to $requestName (response status is not OK)", e)
            }
        }
    }

    override fun dispose() {
        client.close()
    }
}

data class UserInfo(
    val displayName: String = "default",
    val level: Int = 1,
)

data class Notification(
    val text: String,
)
