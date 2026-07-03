package dev.tanuki.core.domain.auth

/** OAuth tokens as returned by GitLab's /oauth/token endpoint. */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    /** Absolute expiry (epoch seconds) computed from `created_at + expires_in`. */
    val expiresAtEpochSeconds: Long,
)
