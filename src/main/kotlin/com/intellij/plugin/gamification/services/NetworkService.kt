package com.intellij.plugin.gamification.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*

class NetworkService: Disposable {
    companion object {
        fun getInstance() = service<NetworkService>()
    }

    private val url = "http://0.0.0.0:8080"
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 1000
            connectTimeoutMillis = 1000 // когда может случиться?
        }
    }

    suspend fun getUsers(): List<User>? {
        return try {
            client.get("$url/users")
        } catch (e: HttpRequestTimeoutException) {
            null
        }
    }


    override fun dispose() {
        client.close()
    }
}

data class User(
    val id: Int,
    var name: String,
    var points: Int
)
