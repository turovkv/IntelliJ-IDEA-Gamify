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
    class ClientState {
        var userId: Int = -1
        var login: String = "No login"
    }

    private var state = ClientState()
    override fun getState() = state
    override fun loadState(state: ClientState) {
        this.state = state
    }

    companion object {
        fun getInstance() = service<NetworkService>()
    }

    private val url = "http://0.0.0.0:8080"
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 1000 // constant TODO
            connectTimeoutMillis = 1000 // когда может случиться?
        }
        install(Auth) {
            basic {
                username = state.login
                password = getPassword(state.login) ?: "No password" // ??? TODO
            }
        }
    }

    private fun changeCredentials(newUser: String, newPassword: String) {
        val auth = client.feature(Auth)
        if (auth != null) {
            auth.providers.removeAt(0)
            auth.basic {
                username = newUser
                password = newPassword
            }
        } // else TODO
    }

    private fun savePassword(user: String, password: String) {
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
        try {
            val response = client.post<HttpStatement>("$url/users") {
                contentType(ContentType.Application.Json)
                body = UserPasswordCredential(
                    user,
                    password
                )
            }.execute()

            if (response.status != HttpStatusCode.OK) {
                println("Not OK :(") // TODO
                return
            }

            state.userId = response.readText().toInt() // what if not int TODO
            state.login = user
            savePassword(user, password)
            changeCredentials(user, password)
        } catch (e: HttpRequestTimeoutException) {
            println(e.stackTrace) // TODO
        }
    }

    suspend fun getUsers(): List<UserInfo>? {
        try {
            return client.get("$url/users")
        } catch (e: HttpRequestTimeoutException) {
            return null // TODO
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
