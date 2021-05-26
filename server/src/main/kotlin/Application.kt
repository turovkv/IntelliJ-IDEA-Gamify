package com.intellij.gamify.server

import com.intellij.gamify.server.entities.UserInfo
import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.InMemoryGamifyRepository
import com.intellij.gamify.server.routes.installHashedAuthentication
import com.intellij.gamify.server.routes.registerBasicRoutes
import com.intellij.gamify.server.routes.registerNotificationRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

private const val ADD_PREDEFINED_USERS = true

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("Unused")
fun Application.module() {
    val repository: GamifyRepository = InMemoryGamifyRepository().apply {
        if (ADD_PREDEFINED_USERS) {
            addPredefinedUser("kirill", 1)
            addPredefinedUser("katya", 2)
            addPredefinedUser("vitaliy", 3)
            addPredefinedUser("alexey", 4)
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
        gson {
            setPrettyPrinting()
        }
    }

    installHashedAuthentication(repository)

    routing {
        get("/{name}") {
            println("Hello-------------------")
            val name = call.parameters["name"]
            println(name)
            call.respondText("Hello World!")
            return@get
        }
    }
    registerBasicRoutes(repository)
    registerNotificationRoutes()
}

fun GamifyRepository.addPredefinedUser(name: String, level: Int): GamifyRepository {
    val credential = UserPasswordCredential(name, name)
    createUser(credential)
    authenticate(credential)!!.updateUser(UserInfo(name.capitalize(), level))
    return this
}
