package com.intellij.gamify.server.routes

import com.intellij.gamify.server.repository.RepositoryException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.ContentTransformationException
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend inline fun PipelineContext<*, ApplicationCall>.handleResponse(action: () -> Any) {
    try {
        call.respond(action())
    } catch (e: Exception) {
        when (e) {
            is ContentTransformationException,
            is RepositoryException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
            }
            else -> throw e
        }
    }
}
