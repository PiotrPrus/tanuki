package dev.tanuki.core.data.network

import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.EmptyResult
import dev.tanuki.core.domain.util.Result
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Remote> {
    val response = try {
        execute()
    } catch (_: SocketTimeoutException) {
        return Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch (_: UnresolvedAddressException) {
        return Result.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        return Result.Failure(DataError.Remote.UNKNOWN)
    }
    return responseToResult(response)
}

/** For write endpoints whose response body we don't need — only success/failure matters. */
suspend fun safeCallEmpty(execute: suspend () -> HttpResponse): EmptyResult<DataError.Remote> {
    val response = try {
        execute()
    } catch (_: SocketTimeoutException) {
        return Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch (_: UnresolvedAddressException) {
        return Result.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        return Result.Failure(DataError.Remote.UNKNOWN)
    }
    return when (response.status.value) {
        in 200..299 -> Result.Success(Unit)
        401, 403 -> Result.Failure(DataError.Remote.UNAUTHORIZED)
        408 -> Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
        429 -> Result.Failure(DataError.Remote.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Failure(DataError.Remote.SERVER)
        else -> Result.Failure(DataError.Remote.UNKNOWN)
    }
}

/**
 * For paginated list endpoints where we want both the items and the total count.
 * Returns the (possibly capped by per_page) items plus the `X-Total` header value, or null on failure.
 */
suspend inline fun <reified T> listWithTotal(execute: () -> HttpResponse): Pair<List<T>, Int?>? {
    val response = try {
        execute()
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        return null
    }
    if (response.status.value !in 200..299) return null
    val items = try {
        response.body<List<T>>()
    } catch (_: Exception) {
        emptyList()
    }
    return items to response.headers["X-Total"]?.toIntOrNull()
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Remote> =
    when (response.status.value) {
        in 200..299 -> try {
            Result.Success(response.body<T>())
        } catch (_: Exception) {
            Result.Failure(DataError.Remote.SERIALIZATION)
        }
        401 -> Result.Failure(DataError.Remote.UNAUTHORIZED)
        408 -> Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
        429 -> Result.Failure(DataError.Remote.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Failure(DataError.Remote.SERVER)
        else -> Result.Failure(DataError.Remote.UNKNOWN)
    }
