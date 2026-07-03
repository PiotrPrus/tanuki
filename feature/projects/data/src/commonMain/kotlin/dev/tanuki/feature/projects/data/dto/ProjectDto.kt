package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.Project
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: Long,
    val name: String,
    @SerialName("path_with_namespace") val pathWithNamespace: String,
    val description: String? = null,
    @SerialName("star_count") val starCount: Int = 0,
    @SerialName("last_activity_at") val lastActivityAt: String? = null,
    @SerialName("web_url") val webUrl: String,
)

fun ProjectDto.toProject(): Project = Project(
    id = id,
    name = name,
    pathWithNamespace = pathWithNamespace,
    description = description,
    starCount = starCount,
    lastActivity = lastActivityAt,
    webUrl = webUrl,
)
