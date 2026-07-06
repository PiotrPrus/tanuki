package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.Group
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: Long,
    val name: String,
    @SerialName("full_path") val fullPath: String,
    val description: String? = null,
    @SerialName("parent_id") val parentId: Long? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

fun GroupDto.toGroup(): Group = Group(
    id = id,
    name = name,
    fullPath = fullPath,
    description = description?.takeIf { it.isNotBlank() },
    parentId = parentId,
    avatarUrl = avatarUrl,
)
