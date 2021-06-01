package com.intellij.plugin.gamification.services.network

import com.intellij.openapi.Disposable
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
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.contentType

@Suppress("TooManyFunctions")
class KtorClientImpl : Client, Disposable {
    companion object {
        private const val url = "http://0.0.0.0:8080"
        private const val requestTimeoutMillisConst: Long = 1000
        private const val connectTimeoutMillisConst: Long = 1000
    }
    private val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeoutMillisConst
            connectTimeoutMillis = connectTimeoutMillisConst
        }
        install(Auth)
    }

    private fun isSignedIn(): Boolean = httpClient.feature(Auth)?.providers?.isNotEmpty() ?: false

    override suspend fun getUserByName(name: String): User =
        requestHandler("getUserByName") {
            httpClient.get("$url/users/$name") // is it ok to put data in url ?
        }

    override suspend fun getAllUsers(): List<User> =
        requestHandler("getAllUsers") {
            httpClient.get("$url/users")
        }

    override suspend fun signUp(name: String, password: String) {
        requestHandler("signUp") {
            httpClient.post<Unit>("$url/users") {
                contentType(ContentType.Application.Json)
                body = UserPasswordCredential(
                    name,
                    password
                )
            }
        }
        signIn(name, password)
    }

    override fun signIn(newName: String, newPassword: String) { // maybe should check on server
        val auth = httpClient.feature(Auth)
        if (auth != null) {
            auth.providers.removeAt(0)
            auth.basic {
                username = newName
                password = newPassword
            }
        }
    }

    override suspend fun updateUserInfo(userInfo: UserInfo): Unit =
        requestHandler("updateUserInfo", true) {
            httpClient.get("$url/users/update") {
                contentType(ContentType.Application.Json)
                body = userInfo
            }
        }

    override suspend fun getNotifications(): List<Notification> =
        requestHandler("getNotifications", true) {
            httpClient.get("$url/users/notifications")
        }

    override suspend fun addNotification(notification: Notification): Unit =
        requestHandler("addNotification", true) {
            httpClient.post("$url/users/notifications") {
                contentType(ContentType.Application.Json)
                body = notification
            }
        }

    override suspend fun subscribe(nameTo: String): Unit =
        requestHandler("subscribe", true) {
            httpClient.post("$url/users/subscribing/$nameTo")
        }

    override suspend fun unsubscribe(nameFrom: String): Unit =
        requestHandler("subscribe", true) {
            httpClient.delete("$url/users/subscribing/$nameFrom")
        }

    private suspend fun <T> requestHandler(
        requestName: String,
        signedIn: Boolean = false,
        request: suspend () -> T
    ): T {
        if (signedIn && !isSignedIn()) { // maybe server will tell? idk which way is better
            throw ClientException("Failed to $requestName (you are not signed in)")
        }
        try {
            return request()
        } catch (e: HttpRequestTimeoutException) {
            throw ClientException("Failed to $requestName (server did not respond)", e)
        } catch (e: NoTransformationFoundException) { // not sure
            throw ClientException("Failed to $requestName (invalid data)", e)
        } catch (e: ResponseException) {
            val errorMessage: String = e.response.readText()
            if (errorMessage.isNotEmpty()) {
                throw ClientException("Failed to $requestName ($errorMessage)")
            } else {
                throw ClientException("Failed to $requestName (response status is not OK)", e)
            }
        }
    }

    override fun dispose() {
        httpClient.close()
    }
}
