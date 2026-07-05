package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.ProjectDetail
import dev.tanuki.feature.projects.domain.Visibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDetailDto(
    val id: Long,
    val name: String,
    @SerialName("path_with_namespace") val pathWithNamespace: String,
    val description: String? = null,
    val visibility: String = "private",
    @SerialName("star_count") val starCount: Int = 0,
    @SerialName("forks_count") val forksCount: Int = 0,
    @SerialName("open_issues_count") val openIssuesCount: Int = 0,
    @SerialName("default_branch") val defaultBranch: String? = null,
    val statistics: ProjectStatisticsDto? = null,
    @SerialName("web_url") val webUrl: String,
)

@Serializable
data class ProjectStatisticsDto(
    @SerialName("repository_size") val repositorySize: Long? = null,
)

fun ProjectDetailDto.toProjectDetail(): ProjectDetail = ProjectDetail(
    id = id,
    name = name,
    pathWithNamespace = pathWithNamespace,
    description = description,
    visibility = when (visibility) {
        "public" -> Visibility.PUBLIC
        "internal" -> Visibility.INTERNAL
        "private" -> Visibility.PRIVATE
        else -> Visibility.UNKNOWN
    },
    starCount = starCount,
    forksCount = forksCount,
    openIssuesCount = openIssuesCount,
    defaultBranch = defaultBranch,
    repositorySizeBytes = statistics?.repositorySize,
    webUrl = webUrl,
)
