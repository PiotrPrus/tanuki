package dev.tanuki.feature.auth.domain

import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.EmptyResult

/** Everything the UI needs to start an OAuth login: the URL to open plus the secrets to verify the callback. */
data class AuthorizationRequest(
    val url: String,
    val codeVerifier: String,
    val state: String,
)

interface AuthRepository {
    /** Create a fresh PKCE-backed authorization request (URL + verifier + state). */
    fun createAuthorizationRequest(): AuthorizationRequest

    /** Exchange the returned authorization code for tokens and persist them. */
    suspend fun exchangeCodeForTokens(
        code: String,
        codeVerifier: String,
    ): EmptyResult<DataError.Remote>

    /**
     * Log in with a Personal Access Token against [instanceBaseUrl] (e.g. a self-hosted
     * GitLab). Validates the token, sets the active instance, and persists it as the session.
     */
    suspend fun loginWithToken(
        instanceBaseUrl: String,
        token: String,
    ): EmptyResult<DataError.Remote>

    suspend fun isLoggedIn(): Boolean

    suspend fun logout()
}
