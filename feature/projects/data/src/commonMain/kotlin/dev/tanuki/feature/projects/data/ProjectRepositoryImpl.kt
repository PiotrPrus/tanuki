package dev.tanuki.feature.projects.data

import dev.tanuki.core.data.network.listWithTotal
import dev.tanuki.core.data.network.safeCall
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result
import dev.tanuki.core.domain.util.map
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.feature.projects.data.dto.BranchDto
import dev.tanuki.feature.projects.data.dto.BranchMrRefDto
import dev.tanuki.feature.projects.data.dto.CommitDto
import dev.tanuki.feature.projects.data.dto.PipelineDto
import dev.tanuki.feature.projects.data.dto.ProjectDetailDto
import dev.tanuki.feature.projects.data.dto.ProjectDto
import dev.tanuki.feature.projects.data.dto.TagDto
import dev.tanuki.feature.projects.data.dto.toBranch
import dev.tanuki.feature.projects.data.dto.toProject
import dev.tanuki.feature.projects.data.dto.toProjectDetail
import dev.tanuki.feature.projects.domain.Branch
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

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
        val activity = async { fetchCommitActivity(base, defaultBranch) }

        val tagsResult = tags.await()
        ProjectStats(
            openMergeRequests = openMergeRequests.await(),
            branches = branches.await(),
            tags = tagsResult?.second,
            latestTag = tagsResult?.first?.firstOrNull()?.name,
            releases = releases.await(),
            contributors = contributors.await(),
            latestPipeline = pipeline.await(),
            commitActivity = activity.await(),
        )
    }

    override suspend fun getBranches(projectId: Long): Result<List<Branch>, DataError.Remote> =
        coroutineScope {
            val base = "projects/$projectId"
            val branchesDeferred = async {
                safeCall<List<BranchDto>> {
                    httpClient.get("$base/repository/branches") { parameter("per_page", 100) }
                }
            }
            // Tag each branch with its open MR, if any (best-effort — failures leave branches untagged).
            val mrRefsDeferred = async {
                safeCall<List<BranchMrRefDto>> {
                    httpClient.get("$base/merge_requests") {
                        parameter("state", "opened")
                        parameter("per_page", 100)
                    }
                }
            }

            var mrByBranch: Map<String, Long> = emptyMap()
            mrRefsDeferred.await().onSuccess { refs ->
                mrByBranch = refs.associate { it.sourceBranch to it.iid }
            }
            branchesDeferred.await().map { dtos ->
                dtos.map { it.toBranch(mrByBranch[it.name]) }
            }
        }

    /**
     * Daily commit counts on [ref] over the last [ACTIVITY_WINDOW_DAYS], oldest bucket first.
     * Reads up to the first 100 commits in the window (one page) — enough for a relative pulse.
     */
    private suspend fun fetchCommitActivity(base: String, ref: String?): List<Int> {
        val now = Clock.System.now()
        val since = now - ACTIVITY_WINDOW_DAYS.days
        // null = call failed / not loaded (→ empty list → baseline); non-null = loaded (even if no commits).
        var commits: List<CommitDto>? = null
        safeCall<List<CommitDto>> {
            httpClient.get("$base/repository/commits") {
                if (ref != null) parameter("ref_name", ref)
                parameter("since", since.toString())
                parameter("per_page", 100)
            }
        }.onSuccess { commits = it }
        val loaded = commits ?: return emptyList()

        val buckets = IntArray(ACTIVITY_WINDOW_DAYS)
        loaded.forEach { commit ->
            val ts = commit.createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return@forEach
            val daysAgo = (now - ts).inWholeDays.toInt()
            if (daysAgo in 0 until ACTIVITY_WINDOW_DAYS) {
                buckets[ACTIVITY_WINDOW_DAYS - 1 - daysAgo]++
            }
        }
        return buckets.toList()
    }

    private companion object {
        const val ACTIVITY_WINDOW_DAYS = 14
    }
}

private fun String?.toPipelineStatus(): PipelineStatus? = when (this) {
    null -> null
    "success" -> PipelineStatus.SUCCESS
    "failed" -> PipelineStatus.FAILED
    "running", "pending", "created", "preparing", "waiting_for_resource", "scheduled" -> PipelineStatus.RUNNING
    else -> PipelineStatus.OTHER
}
