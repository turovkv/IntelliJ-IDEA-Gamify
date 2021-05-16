package com.intellij.gamify.server.routes

import com.intellij.gamify.server.entities.UserInfo
import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.RepositoryException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.basicRouting(repository: GamifyRepository) {
    route("/users") {
        get("") {
            call.respond(repository.getAllUserInfos())
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

            try {
                val user = repository.getUserById(id)
                call.respond(user)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    e.localizedMessage
                )
            }
        }

        post("") {
            val userInfo = call.receive<UserInfo>()

            try {
                val id: Int = repository.addUser(userInfo)
                call.respond(id)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
                return@post
            }
        }

        put("{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!"
                )
                return@put
            }

            try {
                val userInfo = call.receive<UserInfo>()
                repository.updateUser(userId, userInfo)
                call.respond(HttpStatusCode.OK)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    e.localizedMessage
                )
            }
        }

        delete("{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!"
                )
                return@delete
            }

            try {
                repository.deleteUser(userId)
                call.respond(HttpStatusCode.OK)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "found no user with the id $userId"
                )
            }
        }
    }
}

fun Application.registerBasicRoutes(repository: GamifyRepository) {
    routing {
        basicRouting(repository)
    }
}
