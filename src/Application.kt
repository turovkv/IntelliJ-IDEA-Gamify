package com.intellij.gamify.server

import com.intellij.gamify.server.entities.UserDraft
import com.intellij.gamify.server.repository.InMemoryGamifyRepository
import com.intellij.gamify.server.repository.GamifyRepository
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {

        val repository: GamifyRepository = InMemoryGamifyRepository()

        get("/") {
            call.respondText("Hello World!")
        }

        get("/users") {
            call.respond(repository.getAllUsers())
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number"
                )
                return@get
            }

            val user = repository.getUser(id)

            if (user == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "found no user for the provided id $id"
                )
            } else {
                call.respond(user)
            }
        }

        post("/users") {
            val userDraft = call.receive<UserDraft>()
            val user = repository.addUser(userDraft)
            call.respond(user)
        }

        put("/users/{id}") {
            val userDraft = call.receive<UserDraft>()
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest,
                    "id parameter has to be a number!")
                return@put
            }

            val updated = repository.updateUser(userId, userDraft)
            if (updated) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound,
                "found no user with the id $userId")
            }
        }

        delete("/users/{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest,
                    "id parameter has to be a number!")
                return@delete
            }

            val removed = repository.removeUser(userId)
            if (removed) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound,
                    "found no user with the id $userId")
            }
        }
    }
}
