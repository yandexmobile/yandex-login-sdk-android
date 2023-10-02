package com.yandex.authsdk

import java.io.IOException

class YandexAuthException : Exception {

    val errors: Array<String>

    constructor(error: String) : this(arrayOf<String>(error))

    constructor(errors: Array<String>) : super(errors.contentToString()) {
        this.errors = errors
    }

    constructor(e: IOException) : super(CONNECTION_ERROR, e) {
        errors = arrayOf(CONNECTION_ERROR)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val error = other as YandexAuthException

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return errors.contentEquals(error.errors)
    }

    override fun hashCode(): Int {
        return errors.contentHashCode()
    }

    companion object {

        const val CONNECTION_ERROR = "connection.error"

        const val SECURITY_ERROR = "security.error"

        const val JWT_AUTHORIZATION_ERROR = "jwt.authorization.error"

        const val UNKNOWN_ERROR = "unknown.error"
    }
}
