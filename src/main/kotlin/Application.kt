package com.intellij.gamify.server

import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.InMemoryGamifyRepository
import com.intellij.gamify.server.routes.registerBasicRoutes
import com.intellij.gamify.server.routes.registerNotificationRoutes
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("Unused")
fun Application.module() {

    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    val repository: GamifyRepository = InMemoryGamifyRepository()


    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    registerBasicRoutes(repository)
    registerNotificationRoutes(repository)
}
