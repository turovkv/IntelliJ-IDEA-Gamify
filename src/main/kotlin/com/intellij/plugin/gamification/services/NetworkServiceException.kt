package com.intellij.plugin.gamification.services

class NetworkServiceException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
