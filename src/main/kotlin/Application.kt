package com.intellij.gamify.server

import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.InMemoryGamifyRepository
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

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("Unused")
fun Application.module() {
    val repository: GamifyRepository = InMemoryGamifyRepository()

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
    install(Authentication) {
        basic("auth-basic-hashed") {
            validate { credentials ->
                repository.authenticate(credentials)
            }
        }
    }



    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    registerBasicRoutes(repository)
    registerNotificationRoutes(repository)
}
