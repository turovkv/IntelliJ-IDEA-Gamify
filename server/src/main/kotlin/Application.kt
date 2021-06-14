package com.intellij.gamify.server

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.intellij.gamify.server.entities.shared.UserInfo
import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.MongoImplRepository
import com.intellij.gamify.server.routes.installHashedAuthentication
import com.intellij.gamify.server.routes.registerBasicRoutes
import com.intellij.gamify.server.routes.registerNotificationRoutes
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.UserPasswordCredential
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.util.error
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private const val ADD_PREDEFINED_USERS = false

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("Unused")
fun Application.module() {
    configureLogging()

    val repository: GamifyRepository = MongoImplRepository().apply {
        if (ADD_PREDEFINED_USERS) {
            runBlocking {
                addPredefinedUser("kirill", 1)
                addPredefinedUser("katya", 2)
                addPredefinedUser("vitaliy", 3)
                addPredefinedUser("alexey", 4)
            }
        }
    }

    install(CallLogging)
    install(DefaultHeaders)
    install(StatusPages) {
        exception<Throwable> { cause ->
            environment.log.error(cause)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    install(ContentNegotiation) {
        json()
    }

    installHashedAuthentication(repository)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    registerBasicRoutes(repository)
    registerNotificationRoutes()
}

private fun configureLogging() {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = loggerContext.getLogger("org.mongodb.driver")
    rootLogger.level = Level.OFF
}

suspend fun GamifyRepository.addPredefinedUser(name: String, level: Int): GamifyRepository {
    val credential = UserPasswordCredential(name, name)
    createUser(credential)
    authenticate(credential)!!.updateUserInfo(UserInfo(name.capitalize(), level))
    return this
}
