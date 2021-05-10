package com.intellij.gamify.server.routes

import com.intellij.gamify.server.entities.UserInfo
import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.InMemoryGamifyRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.basicRouting(repository: GamifyRepository) {
    route("/users") {
        get("") {
            call.respond(repository.getAllUsers())
        }

        get("{id}") {
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

        post("") {
            val userDraft = call.receive<UserInfo>()
            val user = repository.addUser(userDraft)
            call.respond(user.id)
        }

        put("{id}") {
            val userDraft = call.receive<UserInfo>()
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!")
                return@put
            }

            val updated = repository.updateUser(userId, userDraft)
            if (updated) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    "found no user with the id $userId")
            }
        }

        delete("{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!")
                return@delete
            }

            val removed = repository.removeUser(userId)
            if (removed) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    "found no user with the id $userId")
            }
        }
    }
}


fun Application.registerBasicRoutes(repository: GamifyRepository) {
    routing {
        basicRouting(repository)
    }
}
