package dev.tanuki.feature.auth.data.dto

import kotlinx.serialization.Serializable

/** Minimal shape of GET /user, used to validate a Personal Access Token. */
@Serializable
data class UserDto(
    val id: Long,
    val username: String,
)
