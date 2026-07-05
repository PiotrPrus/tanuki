package dev.tanuki.feature.projects.data

import dev.tanuki.core.data.network.listWithTotal
import dev.tanuki.core.data.network.safeCall
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result
import dev.tanuki.core.domain.util.map
import dev.tanuki.feature.projects.data.dto.PipelineDto
import dev.tanuki.feature.projects.data.dto.ProjectDetailDto
import dev.tanuki.feature.projects.data.dto.ProjectDto
import dev.tanuki.feature.projects.data.dto.TagDto
import dev.tanuki.feature.projects.data.dto.toProject
import dev.tanuki.feature.projects.data.dto.toProjectDetail
import dev.tanuki.feature.projects.domain.PipelineStatus
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectDetail
import dev.tanuki.feature.projects.domain.ProjectFilter
import dev.tanuki.feature.projects.domain.ProjectRepository
import dev.tanuki.feature.projects.domain.ProjectStats
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement

class ProjectRepositoryImpl(
    private val httpClient: HttpClient,
) : ProjectRepository {

    override suspend fun getProjects(filter: ProjectFilter): Result<List<Project>, DataError.Remote> =
        safeCall<List<ProjectDto>> {
            httpClient.get("projects") {
                when (filter) {
                    ProjectFilter.ALL, ProjectFilter.SHARED -> parameter("membership", true)
                    ProjectFilter.STARRED -> parameter("starred", true)
                    ProjectFilter.PERSONAL -> parameter("owned", true)
                }
                parameter("order_by", "last_activity_at")
                parameter("per_page", 30)
            }
        }.map { dtos ->
            dtos.map { it.toProject() }
                // "Shared" = projects you're a member of that live under a group (not your own).
                .filter { filter != ProjectFilter.SHARED || it.namespaceKind == "group" }
        }

    override suspend fun getProject(projectId: Long): Result<ProjectDetail, DataError.Remote> =
        safeCall<ProjectDetailDto> {
            httpClient.get("projects/$projectId") {
                parameter("statistics", true)
            }
        }.map { it.toProjectDetail() }

    override suspend fun getProjectStats(
        projectId: Long,
        defaultBranch: String?,
    ): ProjectStats = coroutineScope {
        val base = "projects/$projectId"

        val openMergeRequests = async {
            listWithTotal<JsonElement> {
                httpClient.get("$base/merge_requests") {
                    parameter("state", "opened")
                    parameter("per_page", 1)
                }
            }?.second
        }
        val branches = async {
            listWithTotal<JsonElement> {
                httpClient.get("$base/repository/branches") { parameter("per_page", 1) }
            }?.second
        }
        val tags = async {
            listWithTotal<TagDto> {
                httpClient.get("$base/repository/tags") {
                    parameter("per_page", 1)
                    parameter("order_by", "updated")
                    parameter("sort", "desc")
                }
            }
        }
        val releases = async {
            listWithTotal<JsonElement> {
                httpClient.get("$base/releases") { parameter("per_page", 1) }
            }?.second
        }
        val contributors = async {
            listWithTotal<JsonElement> {
                httpClient.get("$base/repository/contributors") { parameter("per_page", 1) }
            }?.second
        }
        val pipeline = async {
            listWithTotal<PipelineDto> {
                httpClient.get("$base/pipelines") {
                    if (defaultBranch != null) parameter("ref", defaultBranch)
                    parameter("per_page", 1)
                }
            }?.first?.firstOrNull()?.status.toPipelineStatus()
        }

        val tagsResult = tags.await()
        ProjectStats(
            openMergeRequests = openMergeRequests.await(),
            branches = branches.await(),
            tags = tagsResult?.second,
            latestTag = tagsResult?.first?.firstOrNull()?.name,
            releases = releases.await(),
            contributors = contributors.await(),
            latestPipeline = pipeline.await(),
        )
    }
}

private fun String?.toPipelineStatus(): PipelineStatus? = when (this) {
    null -> null
    "success" -> PipelineStatus.SUCCESS
    "failed" -> PipelineStatus.FAILED
    "running", "pending", "created", "preparing", "waiting_for_resource", "scheduled" -> PipelineStatus.RUNNING
    else -> PipelineStatus.OTHER
}
