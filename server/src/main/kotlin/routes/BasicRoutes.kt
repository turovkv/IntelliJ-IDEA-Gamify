package com.intellij.gamify.server.routes

import com.intellij.gamify.server.entities.UserInfo
import com.intellij.gamify.server.repository.GamifyRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing

private fun Route.basicRouting(repository: GamifyRepository) {
    route("/users") {
        //getAllUserInfos
        get("") {
            handleResponse {
                return@handleResponse repository.getAllUsers()
            }
        }

        //getUserInfoByName
        get("{name}") {
            handleResponse {
                val name = call.parameters["name"]!!
                return@handleResponse repository.getUserByName(name)
            }
        }

        //createUser
        post("") {
            handleResponse {
                val credential = call.receive<UserPasswordCredential>()
                repository.createUser(credential)
                return@handleResponse HttpStatusCode.OK
            }
        }
    }
}

private fun Route.basicRoutingWithAuth() {
    route("/users") {
        //updateUser
        put("update") {
            handleResponse {
                val userInfo = call.receive<UserInfo>()
                repository.updateUserInfo(userInfo)
                return@handleResponse HttpStatusCode.OK
            }
        }
    }
}


fun Application.registerBasicRoutes(repository: GamifyRepository) {
    routing {
        basicRouting(repository)
        authenticateByHash {
            basicRoutingWithAuth()
        }
    }
}
