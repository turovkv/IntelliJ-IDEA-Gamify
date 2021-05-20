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
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.feature
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
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
        var userId: Int = -1
        var login: String = "No login"
    }

    private var state = ClientState()
    override fun getState() = state
    override fun loadState(state: ClientState) {
        this.state = state
    }

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
            throw NetworkServiceException("Auth not installed on HttpClient")
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
        val response: HttpResponse
        try {
            response = client.post<HttpStatement>("$url/users") {
                contentType(ContentType.Application.Json)
                body = UserPasswordCredential(
                    user,
                    password
                )
            }.execute()
        } catch (e: HttpRequestTimeoutException) {
            throw NetworkServiceException("Failed to sign up, server didnt respond", e)
        }

        if (response.status != HttpStatusCode.OK) {
            throw NetworkServiceException("Failed to sign up, response status ${response.status}")
        }

        try {
            state.userId = response.readText().toInt()
        } catch (e: NumberFormatException) {
            throw NetworkServiceException(
                "Failed to sign up, response is not an userId, its \"${response.readText()}\"", e
            )
        }

        state.login = user
        setPassword(user, password)
        changeClientCredentials(user, password)
    }

    suspend fun getUsersInfos(): List<UserInfo> {
        try {
            return client.get("$url/users")
        } catch (e: HttpRequestTimeoutException) {
            throw NetworkServiceException("Failed to getUsersInfos (timeout)", e)
        } catch (e: NoTransformationFoundException) { // not sure
            throw NetworkServiceException("Failed to getUsersInfos (invalid data)", e)
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
