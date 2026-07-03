package dev.tanuki.core.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of a `grant_type=refresh_token` call to /oauth/token. */
@Serializable
data class TokenRefreshDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("created_at") val createdAt: Long,
)
