package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.Visibility
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
    val visibility: String = "private",
    val namespace: NamespaceDto? = null,
)

@Serializable
data class NamespaceDto(
    val kind: String = "user",
)

fun ProjectDto.toProject(): Project = Project(
    id = id,
    name = name,
    pathWithNamespace = pathWithNamespace,
    description = description,
    starCount = starCount,
    lastActivity = lastActivityAt,
    webUrl = webUrl,
    visibility = when (visibility) {
        "public" -> Visibility.PUBLIC
        "internal" -> Visibility.INTERNAL
        "private" -> Visibility.PRIVATE
        else -> Visibility.UNKNOWN
    },
    namespaceKind = namespace?.kind ?: "user",
)
