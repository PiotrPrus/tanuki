package dev.tanuki.core.domain.util

sealed interface DataError : Error {
    enum class Remote : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER,
        SERIALIZATION,
        UNAUTHORIZED,
        UNKNOWN,
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN,
    }
}
